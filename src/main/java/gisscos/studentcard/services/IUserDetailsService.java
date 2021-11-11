package gisscos.studentcard.services;

import java.security.Principal;

public interface IUserDetailsService {

    boolean isSecurityOfficer(Principal principal);

    boolean isUniversity(Principal principal);

    boolean isSuperUser(Principal principal);

    boolean isStudent(Principal principal);

    boolean removeOldValidations();
}
