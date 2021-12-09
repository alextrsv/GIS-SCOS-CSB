package ru.edu.online.services;

import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.OrganizationProfileDTO;
import ru.edu.online.entities.dto.UsersDTO;
import ru.edu.online.entities.dto.UserDTO;

import java.util.Optional;

public interface IScosAPIService {

    Optional<OrganizationDTO[]> getOrganizations();

    Optional<OrganizationProfileDTO> getOrganizationByGlobalId(String globalId);

    Optional<OrganizationDTO> getOrganization(String idOrOGRN);

    Optional<UserDTO> getUserDetails(String userId);

    Optional<UserDTO> getUserByEmail(String email);

    Optional<UsersDTO> getUserByFIO(String firstName, String lastName);
}
