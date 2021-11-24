package ru.edu.online.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.dto.UserDetailsDTO;

import java.security.Principal;
import java.time.Duration;

@Slf4j
public class ScosApiUtils {

    private static final long REQUEST_TIMEOUT = 3000;

    /**
     * Запрос на получение всех организаций СЦОСа
     * @param scosApiClient клиент для отправки запроса
     * @return массив организаций
     */
    public static OrganizationDTO[] getOrganizations(WebClient scosApiClient) {
        return scosApiClient.get()
                .uri(String.join("", "/organizations"))
                .retrieve()
                .bodyToMono(OrganizationDTO[].class)
                .block();
    }

    /**
     * Запрос на получение организации по id
     * @param scosApiClient клиент для отправки запроса
     * @param globalId идентификатор организации
     * @return организация
     */
    public static OrganizationDTO getOrganizationByGlobalId(WebClient scosApiClient, String globalId) {
        return scosApiClient.get()
                .uri(String.join("", "/organizations/?global_id=", globalId))
                .retrieve()
                .bodyToMono(OrganizationDTO.class)
                .block();
    }

    public static UserDTO getUserDetails(WebClient scosApiClient, Principal principal) {
        return scosApiClient
                .get()
                .uri(String.join("", "/users/", principal.getName()))
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }
}
