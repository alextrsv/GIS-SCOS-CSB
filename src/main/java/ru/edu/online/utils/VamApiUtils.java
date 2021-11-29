package ru.edu.online.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.edu.online.entities.dto.StudentsDTO;

import java.time.Duration;

@Slf4j
public class VamApiUtils {

    private final static long REQUEST_TIMEOUT = 3000;

    /**
     * Получить список студентов по одному параметру
     * @param parameter один из допустимых параметров для поиска:
     * inn - ИНН,
     * snils - СНИЛС,
     * email - почта,
     * scos_id - идентификатор в СЦОСе,
     * study_year - курс обучения студента,
     * filter - фильтр для поиска по ФИО (прим. - ван)
     * organization_id - идентификатор организации в СЦОС или её ОГРН,
     * from_date - дата, начиная с которой, будут отображаться изменения
     * @param value знаение параметра поиска
     * @return результат поиска по заданному параметру
     */
    public static StudentsDTO getStudents(String parameter, String value, WebClient vamApiClient) {
        return vamApiClient
                .get()
                .uri(String.join("", "/students?", parameter, "=", value))
                .retrieve()
                .bodyToMono(StudentsDTO.class)
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block();
    }
}
