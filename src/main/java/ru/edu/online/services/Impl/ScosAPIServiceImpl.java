package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.edu.online.entities.dto.*;
import ru.edu.online.services.IScosAPIService;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class ScosAPIServiceImpl implements IScosAPIService {

    /** Время ожидания ответа на запрос */
    private static final long REQUEST_TIMEOUT = 3000;

    /** Клиент для запросов к СЦОСу */
    private final WebClient devScosApiClient;


    @Autowired
    public ScosAPIServiceImpl(WebClient devScosApiClient) {
        this.devScosApiClient = devScosApiClient;
    }

    /**
     * Запрос на получение всех организаций СЦОСа
     * @return массив организаций
     */
    @Override
    public Optional<OrganizationDTO[]> getOrganizations() {
        return Optional.ofNullable(devScosApiClient.get()
                .uri(String.join("", "/organizations"))
                .retrieve()
                .bodyToMono(OrganizationDTO[].class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block());
    }

    /**
     * Запрос на получение организации по id
     * @param globalId идентификатор организации
     * @return организация
     */
    @Override
    public Optional<OrganizationProfileDTO> getOrganizationByGlobalId(String globalId) {
        return Optional.ofNullable(devScosApiClient.get()
                .uri(String.join("", "/organizations/university/", globalId))
                .retrieve()
                .bodyToMono(OrganizationProfileDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block());
    }

    /**
     * Получить организацию по id или ОГРН
     * @param idOrOGRN id или ОГРН организации
     * @return организация
     */
    @Override
    public Optional<OrganizationDTO> getOrganization(String idOrOGRN) {
        return Optional.ofNullable(devScosApiClient.get()
                .uri(String.join("", "/organizations/", idOrOGRN))
                .retrieve()
                .bodyToMono(OrganizationDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block());
    }

    /**
     * Получить информацию о пользователе со списком ролей
     * @param userId идентификатор польователя
     * @return информация о пользователе
     */
    @Override
    public Optional<UserDTO> getUserDetails(String userId) {
        return Optional.ofNullable(
                makeGetUserDTORequest(String.join("", "/users/", userId))
        );
    }

    /**
     * Получить информацию о пользователе по его почте
     * @param email почта пользователя
     * @return информация о пользователе
     */
    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return Optional.ofNullable(
                makeGetUserDTORequest(String.join("", "/users?email=", email))
        );
    }

    /**
     * Получение списка пользователей по ФИО
     * @param firstName имя
     * @param lastName фамилия
     * @return список пользователей СЦОСа по ФИО
     */
    @Override
    public Optional<UsersDTO> getUserByFIO(String firstName, String lastName) {
        return Optional.ofNullable(devScosApiClient.get()
                .uri(String.join("", "/users?page=0&size=100&query=", lastName, " ", firstName))
                .retrieve()
                .bodyToMono(UsersDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block());
    }

    /**
     * Сделать запрос на получение данных о пользователе
     * @param url адрес
     * @return информация о пользователе
     */
    private UserDTO makeGetUserDTORequest(String url) {
        return devScosApiClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }
}
