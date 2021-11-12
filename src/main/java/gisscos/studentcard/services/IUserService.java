package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.UserDTO;

import java.util.List;

public interface IUserService {

    List<String> getPermittedOrganizations(UserDTO user);

    String getOrganizationsNamesAsString(UserDTO user);

    String getPermittedOrganizationsNamesAsString(UserDTO user);

    String getUserRolesAsString(UserDTO user);

    String makeContent(UserDTO user);

    String makeUsefullContent(UserDTO userDTO);
}
