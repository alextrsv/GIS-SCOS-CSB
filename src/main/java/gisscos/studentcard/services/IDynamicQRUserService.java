package gisscos.studentcard.services;

import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.dto.StudentDTO;

import java.util.List;
import java.util.Set;

public interface IDynamicQRUserService {

    List<DynamicQRUser> addAll(List<StudentDTO> studentDTOList);

    Set<String> getPermittedOrganizations(DynamicQRUser user);
}
