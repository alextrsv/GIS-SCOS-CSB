package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.UserDTO;

import java.util.List;

public interface IStudentService {

    List<String> getPermittedOrganizations(StudentDTO studentDTO);

    String getOrganizationsName(StudentDTO studentDTO);

    String getPermittedOrganizationsNamesAsString(StudentDTO studentDTO);

    String makeContent(StudentDTO studentDTO);

    String makeUsefullContent(StudentDTO studentDTO);
}
