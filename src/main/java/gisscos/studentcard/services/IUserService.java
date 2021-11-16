package gisscos.studentcard.services;

import gisscos.studentcard.entities.dto.UserDTO;

import java.util.Set;

public interface IUserService {

    String getOrganizationsNamesAsString(UserDTO user);

    String getUserRolesAsString(UserDTO user);

    String makeContent(UserDTO user);

    String makeUsefullContent(UserDTO userDTO);
}
