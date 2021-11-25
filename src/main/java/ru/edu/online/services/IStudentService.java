package ru.edu.online.services;

import ru.edu.online.entities.dto.StudentDTO;

import java.util.Set;

public interface IStudentService {

    Set<String> getPermittedOrganizations(StudentDTO studentDTO);

    String getOrganizationsName(StudentDTO studentDTO);

    String makeContent(StudentDTO studentDTO);

    String makeUsefullContent(StudentDTO studentDTO);
}
