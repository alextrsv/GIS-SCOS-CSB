package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.StudentDTO;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {

     List<UUID> getPermittedOrganizations(StudentDTO studentDTO);
}
