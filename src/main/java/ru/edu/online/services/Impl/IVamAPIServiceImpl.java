package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.StudentsDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.services.IVamAPIService;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class IVamAPIServiceImpl implements IVamAPIService {

    private final WebClient devVamApiClient;

    private final static long REQUEST_TIMEOUT = 10000;

    @Autowired
    public IVamAPIServiceImpl(WebClient devVamApiClient) {

        this.devVamApiClient = devVamApiClient;
    }

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
    @Override
    public Optional<StudentsDTO> getStudents(String parameter, String value) {
        return Optional.ofNullable(devVamApiClient
                .get()
                .uri(String.join("", "/students?", parameter, "=", value,
                        "&page_size=100000"))
                .retrieve()
                .bodyToMono(StudentsDTO.class)
                .doOnError(error -> log.error("An error has occurred {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
                .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == 503 ? Mono.empty() : Mono.error(ex))
                .retryWhen(Retry.fixedDelay(5, Duration.ofMillis(REQUEST_TIMEOUT)))
                .block());
    }

    /**
     * Получить студента по его почте
     * @param user информация о пользователе
     * @return студент по почте пользователя из ВАМа
     */
    @Override
    public Optional<StudentDTO> getStudentByEmail(UserDTO user) {
        Optional<StudentsDTO> students = this.getStudents("email", user.getEmail());
        return students.flatMap(studentsDTO -> studentsDTO
                .getResults()
                .stream()
                .filter(student -> student.getEmail().equals(user.getEmail()))
                .findFirst());
    }
}
