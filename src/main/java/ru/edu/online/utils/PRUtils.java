package ru.edu.online.utils;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.PRSearchFilter;
import ru.edu.online.repositories.IPRRepository;
import ru.edu.online.services.IScosAPIService;
import ru.edu.online.services.IUserDetailsService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилитный класс заявок
 */
public class PRUtils {

    /**
     * Получить тип фильтра по строке поиска
     * @param search строка поиска
     * @return тип фильтра (номер заявки, организация)
     */
    public static PRSearchFilter getFilterType(String search) {
        if (search.matches(".*\\d.*")) {
            return PRSearchFilter.NUMBER;
        }

        return PRSearchFilter.ORGANIZATION;
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
        for (PassRequest request : requests) {
            Optional<OrganizationProfileDTO> organizationDTO =
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
    public static Long getRequestNumber(IPRRepository requestRepository) {
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

    /**
     * Получить пользователей из заявок
     * @param requests одобренные заявки
     * @return пользователи заявок
     */
    public static List<UserDTO> getUsersFromPassRequests(List<PassRequest> requests,
                                                         IUserDetailsService userDetailsService) {
        List<UserDTO> users = new LinkedList<>();
        Optional<UserProfileDTO> profile;

        for (PassRequest request : requests) {
            switch (request.getType()) {
                case SINGLE:
                    profile = userDetailsService.getUserProfile(request.getAuthorId());
                    profile.ifPresent(
                            userProfileDTO ->
                                    users.add(
                                            UserUtils.getUserDTOFromUserProfileDTO(
                                                    userProfileDTO,
                                                    request.getAuthorId()
                                            )
                                    )
                    );
                    break;
                case GROUP:
                    for (PassRequestUser user : request.getPassRequestUsers()) {
                        profile = userDetailsService.getUserProfile(user.getScosId());
                        profile.ifPresent(
                                userProfileDTO ->
                                        users.add(
                                                UserUtils.getUserDTOFromUserProfileDTO(
                                                        userProfileDTO,
                                                        user.getScosId()
                                                )
                                        )
                        );
                    }
                    break;
            }
        }

        return users;
    }

    /**
     * Получить заявку по id
     * @param passRequestDTO заявки
     * @return заявка
     */
    public static Optional<PassRequest> getPR(
            PRDTO passRequestDTO,
            IPRRepository passRequestRepository) {
        return passRequestRepository.findById(passRequestDTO.getId());
    }

    /**
     * Получить заявку по id
     * @param passRequestUserDTO пользователя заявки
     * @return заявка
     */
    public static Optional<PassRequest> getPR(
            PRUserDTO passRequestUserDTO,
            IPRRepository passRequestRepository) {
        return passRequestRepository.findById(passRequestUserDTO.getPassRequestId());
    }

    /**
     * Получить азявку по id
     * @param id id заявки
     * @return заявка
     */
    public static Optional<PassRequest> getPR(
            UUID id,
            IPRRepository passRequestRepository) {
        return passRequestRepository.findById(id);
    }
}
