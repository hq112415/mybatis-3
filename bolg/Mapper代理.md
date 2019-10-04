使用mybtais时，我们只需要那个定义一个mapper接口，然后在调用

```java
public void testCase1() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test2/mybatis-config-2.xml"));
        StudentMapper studentDao = sqlSessionManager.getMapper(StudentMapper.class);
        Student stu = studentDao.selectById(1L);
        System.out.println(stu);
    }
```

就可以获得一个接口的代理对象完成需要的操作。下面主要介绍两点内容，1. 代理的实现过程是什么； 2. 为什么使用mapper代理的方式，需要保持mapper配置文件包名、mapper接口包名、配置文件的namespace三者保持一致。

## 1. 代理实现的细节

jdk的动态代理原理，涉及Proxy、InvocationHander的实现细节这里就忽略了，大家应该很熟悉，下面通过代码片段来看看细节

```java
public class SqlSessionManager implements SqlSessionFactory, SqlSession {
  ...
  private final SqlSessionFactory sqlSessionFactory;
  ...1. 
    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }
  ...2.
    @Override
    public Configuration getConfiguration() {
    /**
    * 这里sqlSessionFactory就是DefaultSqlSessionFactory，一个DefaultSqlSessionFactory和一个Configuration对应，
    * Configuration就是解析xml配置文件得到一个配置类
    */
        return sqlSessionFactory.getConfiguration();
    }
}
```

由sqlSession.getMapper来到Configuration

```java
public class Configuration {
  ...
    /**
     * 一个Configuration对应一个mapperRegistry，看名字也知道它存储了Mapper的配置信息，也就是一个mapper.xml对应一个配置信息
     * private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();
     * 它其实就是一个map，一个Class对应一个MapperProxyFactory，例如
     * 一个StudentMapper.class--->MapperProxyFactory<StudentMapper>
     */
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);
  ...
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }
```

来到MapperRegistry

```java
public class MapperRegistry {

    private final Configuration config;
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();
	...
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
          //knownMappers是在解析配置文件的时候就已经放进去的
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }
```

来到MapperProxyFactory

```java
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<>();
  ...
    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }
}
```

到这里其实已经比较明了了，Proxy的代理类代理的接口是mapperInterface，即我们定义StudentMapper接口，

mapperProxy即new MapperProxy得到的

![MapperProxy](../picture/MapperProxy.png)

