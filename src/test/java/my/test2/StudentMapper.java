package my.test2;

import my.model.Student;
import org.apache.ibatis.plugin.Interceptor;

import java.util.List;

public interface StudentMapper {
    Student selectById(Long id);

    List<Student> selectWithPage(Integer page);
}
