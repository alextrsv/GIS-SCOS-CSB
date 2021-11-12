package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.StudentDTO;

import java.util.List;

public interface OrganizationService {

     List<String> getStudentPermittedOrganizations(StudentDTO studentDTO);
}
