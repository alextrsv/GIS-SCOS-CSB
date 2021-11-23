package ru.edu.online.utils;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.OrganizationDTO;
import ru.edu.online.entities.enums.PassRequestSearchFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Утилитный класс заявок
 */
public class PassRequestUtils {

    /**
     * Получить тип фильтра по строке поиска
     * @param search строка поиска
     * @return тип фильтра (номер заявки, организация)
     */
    public static PassRequestSearchFilter getFilterType(String search) {
        if (search.matches(".*\\d.*")) {
            return PassRequestSearchFilter.NUMBER;
        }

        return PassRequestSearchFilter.ORGANIZATION;
    }

    /**
     * Фильтрация заявок по организациям
     * @param requests заявки
     * @param organizationName название организации (частичное или полное)
     * @param client клиент для поиска заявки
     * @return результат поиска
     */
    public static List<PassRequest> filterRequestListByOrganizations(List<PassRequest> requests,
                                                                     String organizationName,
                                                                     WebClient client) {
        List<PassRequest> filteredList = new ArrayList<>();
        Optional<OrganizationDTO> organizationDTO;
        for (PassRequest request : requests) {
            organizationDTO = getOrganizationByPassRequest(request, client);
            if (organizationDTO.isPresent()) {
                if (organizationDTO.get()
                        .getShort_name()
                        .toLowerCase(Locale.ROOT)
                        .contains(
                                organizationName.toLowerCase(Locale.ROOT)
                        )
                ) {
                    filteredList.add(request);
                }
            }
        }

        return filteredList;
    }

    /**
     * Получить организацию заявки
     * @param request заявка
     * @param client клиент для поиска организации в СЦОСе
     * @return организация. Если не найдена, пустая организация
     */
    private static Optional<OrganizationDTO> getOrganizationByPassRequest(PassRequest request, WebClient client) {
        return Optional.ofNullable(client
                .get()
                .uri(
                        String.join(
                                "",
                                "organizations",
                                "?global_id=",
                                request.getUniversityId()
                        )
                ).retrieve()
                        .onStatus(HttpStatus::is4xxClientError,
                                error -> Mono.error(new RuntimeException("Organization not found!"))
                        )
                .bodyToMono(OrganizationDTO.class)
                .doOnError(error -> Mono.justOrEmpty(Optional.empty()))
                .onErrorReturn(new OrganizationDTO("", "", "", "", ""))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(2000)))
                .block());
    }
}
