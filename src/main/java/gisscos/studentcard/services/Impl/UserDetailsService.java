package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.dto.UserDetailsDTO;
import gisscos.studentcard.entities.enums.ScosUserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.security.Principal;
import java.time.Duration;
import java.util.Arrays;

/**
 * Сервис данных пользователя
 */
@Slf4j
@Service
public class UserDetailsService {

    /** Время ожидания в случае отсутствия ответа */
    private static final int REQUEST_TIMEOUT = 1000;
    /** Веб клиент для доступа к АПИ ГИС СЦОСа */
    private final WebClient scosApiClient;

    @Autowired
    public UserDetailsService(WebClient scosApiClient) {
        this.scosApiClient = scosApiClient;
    }

    /**
     * Является ли пользователь охранником?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    public boolean isSecurityOfficer(Principal principal) {
        return hasRole(principal, ScosUserRole.SECURITY_OFFICER);
    }

    /**
     * Является ли пользователь администратором ООВО?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    public boolean isUniversity(Principal principal) {
        return hasRole(principal, ScosUserRole.UNIVERSITY);
    }

    /**
     * Является ли пользователь администратором ГИС СЦОС?
     * @param principal информация о пользователе
     * @return true/false в зависимости от роли пользователя
     */
    public boolean isSuperUser(Principal principal) {
        return hasRole(principal, ScosUserRole.SUPER_USER);
    }

    /**
     * Имеет ли роль
     * @param principal информация о пользователе
     * @param role искомая роль
     * @return true - имеет, false - не имеет
     */
    private boolean hasRole(Principal principal, ScosUserRole role) {
        return Arrays
                .asList(
                        loadUserRoleByIdSync(principal)
                                .getRoles()
                )
                .contains(role.toString());
    }

    /**
     * Синхронный запрос к АПИ ГИС СЦОС на загрузку
     * информации о пользователе по id из principal
     * @param principal информация о пользователе
     * @return dto пользователя из ответа на запрос
     */
    private UserDetailsDTO loadUserRoleByIdSync(final Principal principal) {
        return scosApiClient
                .get()
                .uri(String.join("", "/users/", principal.getName()))
                .retrieve()
                .bodyToMono(UserDetailsDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }
}
