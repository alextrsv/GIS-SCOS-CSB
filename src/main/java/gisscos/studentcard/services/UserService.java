package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.UserDTO;

import java.util.List;

public interface UserService {

    List<String> getPermittedOrganizations(UserDTO user);

    String getOrganizationsNamesAsString(UserDTO user);

    String getPermittedOrganizationsNamesAsString(UserDTO user);

    String getUserRolesAsString(UserDTO user);
}
