package my.test3;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.Test;

public class SqlSessionTemplateTest {


    /**
     * 测试一级缓存  -- 需要关闭二级缓存 移除 <cache/>
     */
    @Test
    public void testCase1() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test3/mybatis-config-3.xml"));
        StudentMapper mapper = sqlSessionManager.getMapper(StudentMapper.class);
        //
        mapper.selectById(1L);
        mapper.selectById(1L);
        /**
         * 这里测试一级缓存，讲道理，mybatis的一级缓存是默认开启的，也没法关闭，但是发现这里打印了两次sql语句
         * 也就是说一级缓存无效了，why?
         * 仔细发现commit()操作又清缓存的效果，但是第一次我并没有执行commit操作啊。。
         * 仔细研究SqlSessionManager的SqlSessionInterceptor你会发现原因了，原来我这里SqlSessionManager用的不规范
         * 前面提到过SqlSessionManager规范的用法是首先startManagedSession(); 开启一个session并存起来，
         * 否则会自动开启一个并且没有放到localSqlSession里面去，所以每次selectById操作相当于都是一次新的sqlSession
         * 规范的用法在testCase2() 或 testCase3()
         */

    }

    @Test
    public void testCase2() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test3/mybatis-config-3.xml"));
        sqlSessionManager.startManagedSession();
        StudentMapper mapper = sqlSessionManager.getMapper(StudentMapper.class);
        mapper.selectById(1L);
        mapper.selectById(1L);
    }

    @Test
    public void testCase3() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test3/mybatis-config-3.xml"));
        SqlSession session1 = sqlSessionManager.openSession();

        session1.getMapper(StudentMapper.class).selectById(1L);
        session1.getMapper(StudentMapper.class).selectById(1L);
    }

    /**
     * 测试二级缓存 -- 注意开启<cache/>
     */
    @Test
    public void testCase4() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test3/mybatis-config-3.xml"));
        SqlSession session1 = sqlSessionManager.openSession();
        SqlSession session2 = sqlSessionManager.openSession();
        SqlSession session3 = sqlSessionManager.openSession();

        session1.getMapper(StudentMapper.class).selectById(1L);
        //
        session2.getMapper(StudentMapper.class).selectById(1L);
        //session1 commit 以后，session3会命中缓存
        session1.commit();

        session3.getMapper(StudentMapper.class).selectById(1L);

    }

}
