package ru.edu.online.controllers;

import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Контроллер для работы с пользовательскими данными
 */
@RestController
@RequestMapping("/user/info")
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
}
