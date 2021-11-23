package ru.edu.online.services;

import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.StudentDTO;

import java.util.List;
import java.util.Set;

public interface IDynamicQRUserService {

    List<DynamicQRUser> addAll(List<StudentDTO> studentDTOList);

    Set<String> getPermittedOrganizations(DynamicQRUser user);

    Set<PassRequest> getAcceptedPassRequests(DynamicQRUser user);
}
