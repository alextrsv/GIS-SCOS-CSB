package ru.edu.online.services;

import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.StudentsDTO;
import ru.edu.online.entities.dto.UserDTO;

import java.util.Optional;

public interface IVamAPIService {

    Optional<StudentsDTO> getStudents(String parameter, String value);

    Optional<StudentDTO> getStudentByEmail(UserDTO user);
}
