package ru.edu.online.services;

import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.dto.UserProfileDTO;
import ru.edu.online.entities.enums.UserRole;

import java.security.Principal;
import java.util.Optional;

public interface IUserDetailsService {

    UserRole getUserRole(Principal principal);

    boolean isSecurityOfficer(Principal principal);

    boolean isUniversity(Principal principal);

    boolean isSuperUser(Principal principal);

    boolean isStudent(Principal principal);

    boolean removeOldValidations();

    Optional<UserProfileDTO> getUserProfile(Principal principal);

    Optional<ResponseDTO> getUsersByOrganization(Principal principal,
                                                 Long page,
                                                 Long pageSize,
                                                 Optional<String> search);

    Optional<String> getAdminOrganizationOGRN(Principal principal);
}
