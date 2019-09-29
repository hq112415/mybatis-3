package my.test2;

import my.model.Student;

public interface StudentMapper {
    Student selectById(Long id);
}
