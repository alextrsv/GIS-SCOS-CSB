package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.dto.UserDetailsDTO;
import ru.edu.online.entities.dto.UserProfileDTO;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.services.IUserDetailsService;

import java.security.Principal;
import java.util.Optional;

/**
 * Контроллер для работы с пользовательскими данными
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    private final IPassRequestService passRequestService;
    private final IUserDetailsService userDetailsService;

    @Autowired
    public UserInfoController(IPassRequestService passRequestService,
                              IUserDetailsService userDetailsService) {
        this.passRequestService = passRequestService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Получить роль пользователя
     * @param principal авторизация пользоватлея
     * @return роль
     */
    @GetMapping("/role")
    public ResponseEntity<UserRole> getRole(Principal principal) {
        return ResponseEntity.ok(userDetailsService.getUserRole(principal.getName()));
    }

    /**
     * Получить профиль пользователя
     * @param principal авторизация пользователя (студент или препод)
     * @return информация для личного кабинета
     */
    @GetMapping("/info")
    public ResponseEntity<UserProfileDTO> getUser(Principal principal,
                                                  @RequestParam(required = false) String userId) {
        if (Optional.ofNullable(userId).isPresent()) {
            return ResponseEntity.of(userDetailsService.getUserProfile(userId));
        }
        return ResponseEntity.of(userDetailsService.getUserProfile(principal.getName()));
    }

    @GetMapping("/access")
    public ResponseEntity<ResponseDTO<PassRequest>> getUserAccesses(Principal principal) {
        return passRequestService.getAcceptedPassRequests(principal.getName()).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDTO<UserDetailsDTO>> getUsersFromAcceptedPassRequestsAdminUniversity(
            Principal principal,
            @RequestParam Long page,
            @RequestParam Long itemsPerPage,
            @RequestParam(required = false) String search) {
        if (userDetailsService.isSuperUser(principal.getName())
                || userDetailsService.isUniversity(principal.getName())) {
            return ResponseEntity.of(
                    passRequestService
                            .getUsersFromAcceptedPassRequestsAdminUniversity(
                                    principal.getName(),
                                    page,
                                    itemsPerPage,
                                    search)
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Получить пользователей из организации админа
     * @param principal авторизация админа
     * @param page номер страницы
     * @param itemsPerPage количество элементов на странице
     * @param search поиск (опционально)
     * @return список пользователей по параметрам
     */
    @GetMapping("/organization")
    public ResponseEntity<ResponseDTO<UserDetailsDTO>> getUserByOrganisation(Principal principal,
                                                                             @RequestParam Long page,
                                                                             @RequestParam Long itemsPerPage,
                                                                             @RequestParam(required = false) String search) {
        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return ResponseEntity.of(
                    userDetailsService.getUsersByOrganization(
                            principal.getName(),
                            page,
                            itemsPerPage,
                            search
                    )
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Получить global_id организации пользователея
     * @param principal атворизхация пользователея
     * @return global_id организации пользователея
     */
    @GetMapping("/")
    public ResponseEntity<String> getUserOrganizationGlobalId(Principal principal) {
        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())
                || userDetailsService.isSecurityOfficer(principal.getName())) {
            return ResponseEntity.of(userDetailsService.getUserOrganizationGlobalId(principal.getName()));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
