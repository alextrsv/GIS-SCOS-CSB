package ru.edu.online.utils;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.dto.OrganizationProfileDTO;
import ru.edu.online.entities.enums.PassRequestSearchFilter;
import ru.edu.online.repositories.IPassRequestRepository;
import ru.edu.online.services.IScosAPIService;

import java.util.*;
import java.util.stream.Collectors;

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
     * Фильтровать заявки
     * @param requests заявки
     * @param search поиск
     * @param scosAPIService сервис для общения со СЦОСом
     * @return список отфильтрованных заявок
     */
    public static List<PassRequest> filterRequest(List<PassRequest> requests,
                                                  String search,
                                                  IScosAPIService scosAPIService) {
        switch (getFilterType(search)) {
            case ORGANIZATION:
                return filterRequestListByOrganizations(
                        requests,
                        search,
                        scosAPIService
                );
            case NUMBER:
                return requests
                        .stream()
                        .filter(
                                request ->
                                        request.getNumber() == Long.parseLong(search)
                        )
                        .collect(
                                Collectors.toList()
                        );
            default:
                return List.of();
        }
    }

    /**
     * Фильтрация заявок по организациям
     * @param requests заявки
     * @param organizationName название организации (частичное или полное)
     * @param scosAPIService сервис для общения со СЦОСом
     * @return результат поиска
     */
    public static List<PassRequest> filterRequestListByOrganizations(List<PassRequest> requests,
                                                                     String organizationName,
                                                                     IScosAPIService scosAPIService) {
        List<PassRequest> filteredList = new ArrayList<>();
        Optional<OrganizationProfileDTO> organizationDTO;
        for (PassRequest request : requests) {
            organizationDTO =
                    scosAPIService.getOrganizationByGlobalId(
                            request.getAuthorUniversityId()
                    );
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
     * Получить номер для новой заявки
     * @param requestRepository репозиторий заявок
     * @return номер новой заявки
     */
    public static Long getRequestNumber(IPassRequestRepository requestRepository) {
        return requestRepository.countAllByNumberGreaterThan(0L) + 1;
    }

    /**
     * Сделать пагинацию для списка заявок с сортировкой по дате создания
     * @param page номер страницы
     * @param requestsPerPage количество запросов на странице
     * @param requests запросы
     * @return страница запросов
     */
    public static List<PassRequest> paginateRequests(List<PassRequest> requests, long page, long requestsPerPage) {
        return requests
                .stream()
                .sorted(Comparator.comparing(PassRequest::getCreationDate).reversed())
                .skip(requestsPerPage * (page - 1))
                .limit(requestsPerPage)
                .collect(Collectors.toList());
    }
}
