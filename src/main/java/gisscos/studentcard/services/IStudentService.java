package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.StudentDTO;

import java.util.Set;

public interface IStudentService {

    Set<String> getPermittedOrganizations(StudentDTO studentDTO);

    String getOrganizationsName(StudentDTO studentDTO);

    String getPermittedOrganizationsNamesAsString(StudentDTO studentDTO);

    String makeContent(StudentDTO studentDTO);

    String makeUsefullContent(StudentDTO studentDTO);
}
