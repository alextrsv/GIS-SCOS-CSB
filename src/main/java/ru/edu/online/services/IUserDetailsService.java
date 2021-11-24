package ru.edu.online.services;

import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.dto.UserDetailsDTO;
import ru.edu.online.entities.dto.UserProfileDTO;
import ru.edu.online.entities.enums.UserRole;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface IUserDetailsService {

    UserRole getUserRole(Principal principal);

    boolean isSecurityOfficer(Principal principal);

    boolean isUniversity(Principal principal);

    boolean isSuperUser(Principal principal);

    boolean isStudent(Principal principal);

    boolean removeOldValidations();

    Optional<UserProfileDTO> getUserProfile(Principal principal);

    Optional<List<UserDetailsDTO>> getUsersByOrganization(Principal principal);
}
