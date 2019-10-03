package my.test1;


import my.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.Test;

import java.io.InputStream;

public class SqlSessionTemplateTest {

    /**
     * 使用SqlSessionFactoryBuilder 方式创建SqlSessionManager
     */
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

    /**
     * 直接创建SqlSessionManager，其实同使用SqlSessionFactoryBuilder区别不大
     */
    @Test
    public void testCase2() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(
                Resources.getResourceAsStream("my/test1/mybatis-config-1.xml"));
        Student stu = sqlSessionManager.selectOne("StudentDao.selectById", 1L);
        System.out.println(stu);
    }

    @Test
    public void testCase3() throws Exception {
        InputStream inputStream = Resources.getResourceAsStream("my/test1/mybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = factory.openSession();
        Student stu = sqlSession.selectOne("selectById", 1L);
        sqlSession.close();
        System.out.println(stu);
    }
}
