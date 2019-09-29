package my.test1;

import my.model.Student;

public interface StudentMapper {
    Student selectById(Long id);
}
