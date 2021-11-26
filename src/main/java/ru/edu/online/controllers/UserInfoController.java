package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.edu.online.entities.dto.UserDetailsDTO;
import ru.edu.online.entities.dto.UserProfileDTO;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IUserDetailsService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для работы с пользовательскими данными
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    private final IUserDetailsService userDetailsService;

    @Autowired
    public UserInfoController(IUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Получить роль пользователя
     * @param principal авторизация пользоватлея
     * @return роль
     */
    @GetMapping("/role")
    public ResponseEntity<UserRole> getRole(Principal principal) {
        return ResponseEntity.ok(userDetailsService.getUserRole(principal));
    }

    @GetMapping("/info")
    public ResponseEntity<UserProfileDTO> getUser(Principal principal) {
        return ResponseEntity.of(userDetailsService.getUserProfile(principal));
    }

    @GetMapping("/organization")
    public ResponseEntity<List<UserDetailsDTO>> getUserByOrganisation(Principal principal,
                                                                      @RequestParam Long page,
                                                                      @RequestParam Long itemsPerPage,
                                                                      @RequestParam(required = false) Optional<String> search) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return ResponseEntity.of(
                    userDetailsService.getUsersByOrganization(
                            principal,
                            page,
                            itemsPerPage,
                            search
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Получить ОГРН организации админа
     * @param principal атворизхация админа
     * @return ОГРН организации админа
     */
    @GetMapping("/")
    public ResponseEntity<String> getAdminOrganizationOGRN(Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return ResponseEntity.of(userDetailsService.getAdminOrganizationOGRN(principal));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
