package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    List<String> getPermittedOrganizations(StudentDTO studentDTO);

    String getOrganizationsName(StudentDTO studentDTO);

    String getPermittedOrganizationsNamesAsString(StudentDTO studentDTO);
}
