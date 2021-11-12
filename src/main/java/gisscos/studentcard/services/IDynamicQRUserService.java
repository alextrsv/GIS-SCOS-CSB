package gisscos.studentcard.services;

import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.dto.StudentDTO;

import java.util.List;

public interface IDynamicQRUserService {

    List<DynamicQRUser> addAll(List<StudentDTO> studentDTOList);

    List<String> getPermittedOrganizations(DynamicQRUser user);
}
