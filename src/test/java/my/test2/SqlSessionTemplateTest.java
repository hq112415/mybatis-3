package my.test2;


import my.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.Test;

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

}
