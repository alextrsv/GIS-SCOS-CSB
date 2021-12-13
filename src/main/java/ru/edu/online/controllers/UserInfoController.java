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
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.dto.UserProfileDTO;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IPRAdminService;
import ru.edu.online.services.IPRUserService;
import ru.edu.online.services.IScosAPIService;
import ru.edu.online.services.IUserDetailsService;

import java.security.Principal;
import java.util.Optional;

/**
 * Контроллер для работы с пользовательскими данными
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {

    /** Сервис заявок администратора */
    private final IPRAdminService passRequestAdminService;
    /** Сервис заявок пользователя */
    private final IPRUserService passRequestUserService;
    /** Сервис данных о пользователях */
    private final IUserDetailsService userDetailsService;
    /** Сервис для работы с АПИ СЦОСа */
    private final IScosAPIService scosAPIService;

    @Autowired
    public UserInfoController(IPRAdminService passRequestAdminService,
                              IPRUserService passRequestUserService,
                              IUserDetailsService userDetailsService,
                              IScosAPIService scosAPIService) {
        this.passRequestAdminService = passRequestAdminService;
        this.passRequestUserService = passRequestUserService;
        this.userDetailsService = userDetailsService;
        this.scosAPIService = scosAPIService;
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

    /**
     * Получить доступы пользователя
     * @param principal авторизация пользователя
     * @return список одобренных заявок (доступов)
     */
    @GetMapping("/access")
    public ResponseEntity<ResponseDTO<PassRequest>> getUserAccesses(Principal principal) {
        if (userDetailsService.isSecurityOfficer(principal.getName())
                || userDetailsService.isUniversity(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestUserService.getAcceptedPassRequests(principal.getName()).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получить список пользователей, которые имеют одобренные заявки
     * в ООВО админа
     * @param principal авторизация админа ООВО
     * @param page номер страницы
     * @param itemsPerPage количество пользователей на странице
     * @param search поиск (опционально)
     * @return список пользователей с доступом в ООВО админа
     */
    @GetMapping("/list")
    public ResponseEntity<ResponseDTO<UserDTO>> getUsersFromAcceptedPassRequestsAdminUniversity(
            Principal principal,
            @RequestParam Long page,
            @RequestParam Long itemsPerPage,
            @RequestParam(required = false) String search) {
        if (userDetailsService.isSuperUser(principal.getName())
                || userDetailsService.isUniversity(principal.getName())) {
            return ResponseEntity.of(
                    passRequestAdminService
                            .getAdminUniversityUsers(
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
    public ResponseEntity<ResponseDTO<UserDTO>> getUserByOrganisation(Principal principal,
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
            return ResponseEntity.of(scosAPIService.getUserOrganizationGlobalId(principal.getName()));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
