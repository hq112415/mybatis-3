package my.test2;


import com.github.pagehelper.PageHelper;
import my.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.Test;

import java.util.List;

public class SqlSessionTemplateTest {

    /**
     * mapper代理方式
     */
    @Test
    public void testCase1() throws Exception {
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(Resources.getResourceAsStream("my/test2/mybatis-config-2.xml"));
        sqlSessionManager.startManagedSession();
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

}
