package ru.edu.online.services;

import ru.edu.online.entities.dto.UserDTO;

public interface IUserService {

    String getOrganizationsNamesAsString(UserDTO user);

    String getUserRolesAsString(UserDTO user);

    String makeContent(UserDTO user);

    String makeUsefullContent(UserDTO userDTO);
}
