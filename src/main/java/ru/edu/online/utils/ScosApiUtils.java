package ru.edu.online.utils;

import org.springframework.web.reactive.function.client.WebClient;
import ru.edu.online.entities.dto.OrganizationDTO;

public class ScosApiUtils {

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
}
