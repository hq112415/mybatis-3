package my.test3;


import com.github.pagehelper.PageHelper;
import my.model.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.Test;

import java.util.List;

public class SqlSessionTemplateTest {


    /**
     * 测试一级缓存
     */
    @Test
    public void testCase3() throws Exception {
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
