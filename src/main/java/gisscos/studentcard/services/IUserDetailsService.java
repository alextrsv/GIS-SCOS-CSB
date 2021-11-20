package gisscos.studentcard.services;

import gisscos.studentcard.entities.enums.UserRole;

import java.security.Principal;

public interface IUserDetailsService {

    UserRole getUserRole(Principal principal);

    boolean isSecurityOfficer(Principal principal);

    boolean isUniversity(Principal principal);

    boolean isSuperUser(Principal principal);

    boolean isStudent(Principal principal);

    boolean removeOldValidations();
}
