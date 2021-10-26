package gisscos.studentcard.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
public class KeyCloakTestController {

    @GetMapping("/anonymous")
    public String getAnonymousInfo() {
        return "Anonymous";
    }

    @GetMapping("/super_user")
    @PreAuthorize("hasRole('SUPER_USER')")
    public String getUserInfo() {
        return "super user info";
    }

    @GetMapping("/security_officer")
    @PreAuthorize("hasRole('SECURITY_OFFICER')")
    public String getAdminInfo() {
        return "security officer info";
    }

    @GetMapping("/university")
    @PreAuthorize("hasRole('UNIVERSITY')")
    public String getServiceInfo() {
        return "university info";
    }

    @GetMapping("/me")
    public Object getMe() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
