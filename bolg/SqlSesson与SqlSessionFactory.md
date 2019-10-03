mybatis核心的核心逻辑都是围绕SqlSession展开的

它的方法包括：

```java
public interface SqlSession extends Closeable {
  ...简写...
	selectOne、selectList、selectMap、selectCursor、select、insert、update、delete、commit()、rollback、getConfiguration
  ...
  <T> T getMapper(Class<T> type);
  ...
  Connection getConnection();
}

```

总得来说就是定义了我们常用的增删改查的方法，注意还有个getMapper这样的一个方法。它的实现类包括

DefaultSqlSession和SqlSessionManager。这里从SqlSessionManager的一个最简单的例子说起

```java
    /**
     * 使用SqlSessionFactoryBuilder 方式创建SqlSessionManager
     */
    @Test
    public void testCase1() throws Exception {
        InputStream inputStream = Resources.getResourceAsStream("my/test1/mybatis-config-1.xml");
        SqlSessionFactory builder = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(builder);
        Student stu = sqlSessionManager.selectOne("StudentDao.selectById", 1L);
        System.out.println(stu);
    }
```

我们项目中为了读写分能的实现并没有用getMapper的方式获取一个代理对象，直接像上面一样调用了selectOne、selectList来做查询的。这里面SqlSessionManager.newInstance的细节摘选如下：

```java
public class SqlSessionManager implements SqlSessionFactory, SqlSession {
    /**
     * 它是产生sqlSession的，实现类为DefaultSqlSessionFactory
     */
    private final SqlSessionFactory sqlSessionFactory;
    /**
     * 看名字这是一个代理
     */
    private final SqlSession sqlSessionProxy;
    /**
     * 可以看作一个缓存的作用，一个线程做重复的sqlSession中的操作时，不需要重复的去new一个DefaultSqlSession
     */
    private final ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<>();

    private SqlSessionManager(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        /*
         *这个代理使用jdk的动态代理实现的，可以看到它代理了的是一个SqlSession的所有操作，至于为什么要这个代理呢，
         * 当然是想在SqlSession的操作前后做点什么事情，从下面的SqlSessionInterceptor的invoke方法中可以看到，它主要做的就是
         * final SqlSession sqlSession = SqlSessionManager.this.localSqlSession.get();
         * 这个操作看来其实就是做了保证一个线程做重复的sqlSession中的操作时，不需要重复的去new一个DefaultSqlSession，这是一个优化
         */
        this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSession.class},
                new SqlSessionInterceptor());
    }
  ...省略...
    @Override
    public void close() {
        final SqlSession sqlSession = localSqlSession.get();
        if (sqlSession == null) {
            throw new SqlSessionException("Error:  Cannot close.  No managed session is started.");
        }
        try {
            sqlSession.close();
        } finally {
            localSqlSession.set(null);
        }
    }

    public void startManagedSession() {
        this.localSqlSession.set(openSession());
    }

    private class SqlSessionInterceptor implements InvocationHandler {
        public SqlSessionInterceptor() {
            // Prevent Synthetic Access
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            /**
             * 需要注意一点，SqlSessionManager的正确的用法是首先
             * manager.startManagedSession();
             * 用完之后需要
             * manager.close();
             *
             * 因为假如你直接manager.selectOne()，这里代理类直接运行到这里，发现sqlSession != null 为false，
             * 然后做openSession操作，最终new一个DefaultSqlSession，但是在else分支种并没有把sqlSession放到localSqlSession中
             * 所以每次的操作都需要就会每次都openSession操作。localSqlSession其实是一个优化
             *
             */
            final SqlSession sqlSession = SqlSessionManager.this.localSqlSession.get();
            if (sqlSession != null) {
                try {
                    return method.invoke(sqlSession, args);
                } catch (Throwable t) {
                    throw ExceptionUtil.unwrapThrowable(t);
                }
            } else {
                try (SqlSession autoSqlSession = openSession()) {
                    try {
                        final Object result = method.invoke(autoSqlSession, args);
                        autoSqlSession.commit();
                        return result;
                    } catch (Throwable t) {
                        autoSqlSession.rollback();
                        throw ExceptionUtil.unwrapThrowable(t);
                    }
                }
            }
        }
    }
```

正确使用SqlSessionManager的方式

```java
    @Test
    public void testCase1() throws Exception {
        InputStream inputStream = Resources.getResourceAsStream("my/test1/mybatis-config-1.xml");
        SqlSessionFactory builder = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSessionManager manager = SqlSessionManager.newInstance(builder);
        manager.startManagedSession();
        Student stu = manager.selectOne("StudentDao.selectById", 1L);
        manager.close();
        System.out.println(stu);
    }
```

SqlSessionManager核心就这些了，下面看SqlSessionFactory，它是由SqlSessionFactoryBuilder().build而来，具体的build细节其实是核心类Configuration的创建过程，即配置文件的解析过程。

```java
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 1. DefaultSqlSessionFactory核心就是生成sqlSession，这个是入口
     */
    @Override
    public SqlSession openSession() {
        /**
         * 默认的
         *protected ExecutorType defaultExecutorType = ExecutorType.SIMPLE;
         */
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }

    /**
     * 2.
     *
     * @param execType   = ExecutorType.SIMPLE;
     * @param level      = null
     * @param autoCommit = false
     * @return
     */
    private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
        Transaction tx = null;
        try {
            /**
             * {@link XMLConfigBuilder#parse()} --> {@link XMLConfigBuilder#parseConfiguration(org.apache.ibatis.parsing.XNode)}
             * -->{@link XMLConfigBuilder#environmentsElement(org.apache.ibatis.parsing.XNode)}
             * 我这里配置的是development，当然可以配置很多个
             */
            final Environment environment = configuration.getEnvironment();
            /**
             * 事务工厂，其实就是environment中配置的那个
             */
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);

            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            /**
             * new了一个executor，默认的是SimpleExecutor，但是我没配置cacheEnabled，它默认是false的，所以其实是CachingExecutor，
             * 这里其实涉及了缓存了，后面看到在再说
             */
            final Executor executor = configuration.newExecutor(tx, execType);
            /**
             *
             */
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            closeTransaction(tx); // may have fetched a connection so lets call close()
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
      ...
    }
       
}
```

核心方法就是openSession了。

