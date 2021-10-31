package gisscos.studentcard.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AuthTestController {

    @GetMapping("/whoami")
    public String whoami(Principal principal) {
        return principal.getName();
    }
}
