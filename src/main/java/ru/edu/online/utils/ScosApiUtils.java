package ru.edu.online.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.dto.UserDTO;

import java.time.Duration;
import java.util.Optional;

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
    public static Optional<OrganizationDTO> getOrganizationByGlobalId(WebClient scosApiClient, String globalId) {
        return Optional.ofNullable(scosApiClient.get()
                .uri(String.join("", "/organizations/?global_id=", globalId))
                .retrieve()
                .bodyToMono(OrganizationDTO.class)
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .block());
    }

    /**
     * Получить организацию по id или ОГРН
     * @param scosApiClient клиент для отправки запроса
     * @param idOrOGRN id или ОГРН
     * @return организация
     */
    public static Optional<OrganizationDTO> getOrganization(WebClient scosApiClient, String idOrOGRN) {
        return Optional.ofNullable(scosApiClient.get()
                .uri(String.join("", "/organizations/", idOrOGRN))
                .retrieve()
                .bodyToMono(OrganizationDTO.class)
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .block());
    }

    /**
     * Получить информацию о пользователе со списком ролей
     * @param scosApiClient клиент для запроса
     * @param userId идентификатор польователя
     * @return информация о пользователе
     */
    public static UserDTO getUserDetails(WebClient scosApiClient, String userId) {
        return scosApiClient
                .get()
                .uri(String.join("", "/users/", userId))
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }

    public static UserDTO getUserByEmail(WebClient scosApiClient, String email) {
        return scosApiClient
                .get()
                .uri(String.join("", "/users?email=", email))
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }
}
