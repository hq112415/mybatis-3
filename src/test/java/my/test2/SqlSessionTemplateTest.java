package my.test2;


import com.github.pagehelper.PageHelper;
import my.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class SqlSessionTemplateTest {

    /**
     * mapper代理方式
     */
    @Test
    public void testCase1() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test2/mybatis-config-2.xml"));
        StudentMapper studentDao = sqlSessionManager.getMapper(StudentMapper.class);
        Student stu = studentDao.selectById(1L);
        System.out.println(stu);
    }

    @Test
    public void testCase() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test2/mybatis-config-2.xml"));
        StudentMapper studentDao = sqlSessionManager.getMapper(StudentMapper.class);
        PageHelper.startPage(1, 10);
        List<Student> list = studentDao.selectWithPage(1);

    }

    /**
     * 测试一级缓存
     */
    @Test
    public void testCase3() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test2/mybatis-config-2.xml"));

        SqlSession session1 = sqlSessionManager.openSession();
        SqlSession session2 = sqlSessionManager.openSession();

        //
        session1.getMapper(StudentMapper.class).selectById(1L);
        session1.close();
        //
        session2.getMapper(StudentMapper.class).selectById(1L);
        session2.close();


    }

}
