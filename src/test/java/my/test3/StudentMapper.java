package my.test3;

import my.model.Student;

import java.util.List;

public interface StudentMapper {
    Student selectById(Long id);

    List<Student> selectWithPage(Integer page);
}
