package ru.edu.online.services;

import ru.edu.online.entities.dto.GenericResponseDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.dto.UserProfileDTO;
import ru.edu.online.entities.enums.UserRole;

import java.util.Optional;

public interface IUserDetailsService {

    UserRole getUserRole(String userId);

    boolean isSecurityOfficer(String userId);

    boolean isUniversity(String userId);

    boolean isSuperUser(String userId);

    boolean isStudent(String userId);

    void makeOldValidationsInvalid();

    Optional<UserProfileDTO> getUserProfile(String userId);

    Optional<GenericResponseDTO<UserDTO>> getUsersByOrganization(String userId,
                                                                 Long page,
                                                                 Long pageSize,
                                                                 String search);

    Optional<String> getUserOrganizationGlobalId(String userId);
}
