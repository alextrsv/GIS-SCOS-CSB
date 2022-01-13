package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestChangeLogEntry;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.PRStatus;
import ru.edu.online.entities.enums.PRStatusForAdmin;
import ru.edu.online.entities.enums.PRType;
import ru.edu.online.repositories.IDynamicQRUserRepository;
import ru.edu.online.repositories.IPRChangeLogRepository;
import ru.edu.online.repositories.IPRRepository;
import ru.edu.online.repositories.IPRUserRepository;
import ru.edu.online.services.IPRAdminService;
import ru.edu.online.services.IPRCommentsService;
import ru.edu.online.services.IScosAPIService;
import ru.edu.online.services.IUserDetailsService;
import ru.edu.online.utils.PRUtils;
import ru.edu.online.utils.UserUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с заявками администратора.
 */
@Service
@Slf4j
@EnableScheduling
public class PRAdminServiceImpl implements IPRAdminService {

    /** Репозиторий истории изменения заявок */
    private final IPRChangeLogRepository passRequestChangeLogRepository;
    /** Репозиторий пользователей заявок */
    private final IPRUserRepository passRequestUserRepository;
    /** Репозиторий динамических QR - кодов */
    private final IDynamicQRUserRepository dynamicQRUserRepository;
    /** Репозиторий заявок */
    private final IPRRepository passRequestRepository;

    /** Сервис комментариев заявок */
    private final IPRCommentsService passRequestCommentsService;
    /** Сервис информации о пользователях */
    private final IUserDetailsService userDetailsService;
    /** Сервис для работы с АПИ СЦОСа */
    private final IScosAPIService scosAPIService;

    @Autowired
    public PRAdminServiceImpl(IPRChangeLogRepository passRequestChangeLogRepository,
                              IPRCommentsService passRequestCommentsService,
                              IPRUserRepository passRequestUserRepository,
                              IDynamicQRUserRepository dynamicQRUserRepository,
                              IPRRepository passRequestRepository,
                              IUserDetailsService userDetailsService,
                              IScosAPIService scosAPIService) {
        this.passRequestChangeLogRepository = passRequestChangeLogRepository;
        this.passRequestCommentsService = passRequestCommentsService;
        this.passRequestUserRepository = passRequestUserRepository;
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.passRequestRepository = passRequestRepository;
        this.userDetailsService = userDetailsService;
        this.scosAPIService = scosAPIService;
    }

    /**
     * Добавление групповой заявки в БД.
     * @param dto DTO заявки
     * @return добавленная заявка
     */
    @Override
    public Optional<PassRequest> addGroupPassRequest(PRDTO dto, String userId) {
        Optional<PassRequest> passRequest = createGroupPassRequest(dto, userId);

        if (passRequest.isPresent()) {
            UUID passRequestId = passRequestRepository.save(passRequest.get()).getId();

            for (PRUserDTO user : dto.getUsers()) {
                user.setPassRequestId(passRequestId);
                addUserToPassRequest(user);
                if (!dynamicQRUserRepository.existsByUserId(user.getUserId()))
                    dynamicQRUserRepository.save(new DynamicQRUser(user.getUserId(), dto.getUniversityId()));
            }
            log.info("group pass request was added");

            passRequestCommentsService.addCommentToPassRequest(
                    new PRCommentDTO(
                            userId,
                            passRequestId,
                            dto.getComment()
                    )
            );
            dto.setId(passRequestId);
            //updatePassRequestStatus(dto);
            return getPR(passRequestId);
        }

        return Optional.empty();
    }

    /**
     * Добавление пользователя в список заявки
     * @param dto dto пользователя заявки
     * @return список всех пользователей, находящихся в заявке
     */
    @Override
    public Optional<List<PassRequestUser>> addUserToPassRequest(PRUserDTO dto) {
        Optional<PassRequest> passRequest = getPR(dto);

        // Если есть такая заявка и она является групповой
        if (passRequest.isPresent() && passRequest.get().getType() == PRType.GROUP) {
            // Если такой пользователь в заявке уже есть
            if (passRequest.get().getPassRequestUsers().stream().anyMatch(user -> user.getScosId().equals(dto.getUserId()))) {
                log.info("the user is already associated to the pass request");
                return Optional.empty();
            }

            PassRequestUser passRequestUser = new PassRequestUser(
                    dto.getPassRequestId(),
                    dto.getUserId(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getPatronymicName(),
                    dto.getPhotoUrl()
            );
            passRequestUserRepository.save(passRequestUser);
            log.info("the user was associated to the pass request successfully");
            return Optional.of(passRequestUserRepository.findAllByPassRequestId(passRequest.get().getId()));
        } else
            log.info("nothing to associate, the pass request type isn't \"GROUP\"");
        return Optional.empty();
    }

    /**
     * Получить пользователей ООВО админа, у которых есть одобренные заявки в его ООВО
     * @param userId идентификатор администратора
     * @param page номер страницы
     * @param usersPerPage количество пользователей на странице
     * @param search поиск по почте (опционально)
     * @return страница пользователей из ООВО админа с одобренными заявками
     */
    public Optional<ResponseDTO<UserDTO>> getAdminUniversityUsers(String userId,
                                                                  long page,
                                                                  long usersPerPage,
                                                                  String search) {
        Optional<String> adminOrganizationGlobalId =
                scosAPIService.getUserOrganizationGlobalId(userId);
        List<PassRequest> acceptedRequestsForUniversity =
                passRequestRepository
                        .findAllByTargetUniversityIdAndStatus(
                                adminOrganizationGlobalId.orElseThrow(),
                                PRStatus.ACCEPTED
                        );
        List<UserDTO> users =
                PRUtils.getUsersFromPassRequests(
                        acceptedRequestsForUniversity,
                        userDetailsService
                );

        return UserUtils.aggregateUserWithPaginationAndSearch(users, page, usersPerPage, search);
    }

    /**
     * Получить количество заявок для админа по статусам
     * @param userId авторизация админа
     * @return мапа: статус - количество
     */
    @Override
    public Optional<Map<PRStatus, Integer>> getPassRequestsCountByStatusForAdmin(String userId) {
        Map<PRStatus, Integer> requestsCountByStatus = new HashMap<>();
        Optional<String> adminUniversityId =
                scosAPIService.getUserOrganizationGlobalId(userId);
        if (adminUniversityId.isPresent()) {
            List<PassRequest> allUniversityRequests =
                    passRequestRepository.findAllByTargetUniversityId(adminUniversityId.get());
            allUniversityRequests.removeIf(request ->
                    request.getStatus() == PRStatus.ACCEPTED && request.getChangeLog().isEmpty()
            );
            for (PRStatus status : PRStatus.values()) {
                requestsCountByStatus.put(
                        status,
                        (int) allUniversityRequests.stream().filter(passRequest -> passRequest.getStatus() == status).count()
                );
            }
        }

        return Optional.of(requestsCountByStatus);
    }

    /**
     * Получить заявки для администратора
     * @param status    стутус заявок
     * @param page      номер страницы (по умолчанию по 5 заявок для админа)
     * @param pageSize  размер страницы
     * @param search    поиск по заявкам. Возможен по названию ООВО и номеру заявки
     * @param userId    идентификатор администратора
     * @return отобранные по критериям заявки
     */
    @Override
    public Optional<ResponseDTO<PassRequest>> getPassRequestsForAdmin(String status,
                                                                      Long page,
                                                                      Long pageSize,
                                                                      String search,
                                                                      String userId) {
        Optional<String> adminUniversityId =
                scosAPIService.getUserOrganizationGlobalId(userId);
        if (adminUniversityId.isPresent()) {
            List<PassRequest> allUniversityRequests =
                    passRequestRepository.findAllByTargetUniversityId(adminUniversityId.get());
            allUniversityRequests.removeIf(request ->
                    request.getStatus() == PRStatus.ACCEPTED && request.getChangeLog().isEmpty()
            );

            switch (PRStatusForAdmin.of(status)) {
                case PROCESSED:
                    return Optional.of(getProcessedPassRequests(allUniversityRequests, page, pageSize, search));
                case IN_PROCESSING:
                    return Optional.of(getPassRequestsInProcessing(allUniversityRequests, page, pageSize, search));
                case FOR_PROCESSING:
                    return Optional.of(getPassRequestsForProcessing(allUniversityRequests, page, pageSize, search));
                case EXPIRED:
                    return Optional.of(getExpiredPassRequests(allUniversityRequests, page, pageSize, search));
                case CREATED:
                    return Optional.of(getCreatedPassRequests(allUniversityRequests, page, pageSize, search, userId));
                default:
                    return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Получение заявок для обработки.
     * @param requests  заявки
     * @param page      номер страницы
     * @param pageSize  размер страницы
     * @param search    поиск
     * @return список заявок в обработке
     */
    private ResponseDTO<PassRequest> getPassRequestsForProcessing(
            List<PassRequest> requests,
            Long page,
            Long pageSize,
            String search) {
        log.info("collect requests sent for consideration to the target OOVO");
        return getPRByStatusWithPaginationAndSearchForUniversity(
                new PRStatus[]{PRStatus.TARGET_ORGANIZATION_REVIEW},
                requests,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение заявок в обработке.
     * @param requests  заявки
     * @param page      номер страницы
     * @param pageSize  размер страницы
     * @param search    поиск
     * @return список заявок в обработке
     */
    private ResponseDTO<PassRequest> getPassRequestsInProcessing(
            List<PassRequest> requests,
            Long page,
            Long pageSize,
            String search) {
        log.info("collect requests sent in consideration to the target OOVO");

        return getPRByStatusWithPaginationAndSearchForUniversity(
                new PRStatus[]{PRStatus.PROCESSED_IN_TARGET_ORGANIZATION},
                requests,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение просроченных заявок.
     * @param requests  заявки
     * @param page      номер страницы
     * @param pageSize  размер страницы
     * @param search    поиск
     * @return список обработанных заявок
     */
    private ResponseDTO<PassRequest> getExpiredPassRequests(
            List<PassRequest> requests,
            Long page,
            Long pageSize,
            String search) {
        log.info("collect expired requests sent for to the OOVO");
        return getPRByStatusWithPaginationAndSearchForUniversity(
                new PRStatus[]{PRStatus.EXPIRED},
                requests,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение обработанных заявок.
     * @param requests  заявки
     * @param page      номер страницы
     * @param pageSize  размер страницы
     * @param search    поиск
     * @return список обработанных заявок
     */
    private ResponseDTO<PassRequest> getProcessedPassRequests(
            List<PassRequest> requests,
            Long page,
            Long pageSize,
            String search) {

        log.info("collect considered requests sent for to the OOVO");
        return getPRByStatusWithPaginationAndSearchForUniversity(
                new PRStatus[]{
                        PRStatus.ACCEPTED,
                        PRStatus.REJECTED_BY_TARGET_ORGANIZATION,
                },
                requests,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение созданных администратором заявок.
     * @param requests  заявки
     * @param page      номер страницы
     * @param pageSize  размер страницы
     * @param search    поиск
     * @return список обработанных заявок
     */
    private ResponseDTO<PassRequest> getCreatedPassRequests(
            List<PassRequest> requests,
            Long page,
            Long pageSize,
            String search,
            String adminId) {

        log.info("collect created requests by admin");

        // Оставляем только те заявки, которые были созданы админом
        requests = requests
                .stream()
                .filter(request -> Objects.equals(adminId, request.getAuthorId()))
                .collect(Collectors.toList());

        return getPRByStatusWithPaginationAndSearchForUniversity(
                PRStatus.values(),
                requests,
                page,
                pageSize,
                search
        );
    }

    /**
     * Обновление статуса заявки
     * @param dto DTO обновленной заявки
     * @return обновленная заявка
     */
    @Override
    public Optional<PassRequest> updatePassRequestStatus(PRDTO dto) {
        Optional<PassRequest> passRequest = getPR(dto);

        if (passRequest.isPresent()) {
            addChangeLogEntryToPassRequest(passRequest.get(), dto.getStatus());
            log.info("pass request with id: {} was updated", dto.getId());
            return getPR(passRequest.get().getId());
        } else
            log.warn("pass request with id: {} not found", dto.getId());
        return Optional.empty();
    }

    /**
     * Обновить даты действия заявки
     * @param PRDTO DTO обновленной заявки
     * @return обновлённая заявка
     */
    @Override
    public Optional<PassRequest> updatePassRequestDates(PRDTO PRDTO) {
        Optional<PassRequest> passRequest = getPR(PRDTO.getId());
        if (passRequest.isPresent()) {
            if (Optional.ofNullable(PRDTO.getStartDate()).isPresent()) {
                passRequest.get().setStartDate(PRDTO.getStartDate());
            }
            if (Optional.ofNullable(PRDTO.getEndDate()).isPresent()) {
                passRequest.get().setEndDate(PRDTO.getEndDate());
            }

            passRequestRepository.save(passRequest.get());
            return passRequest;
        }
        return Optional.empty();
    }

    /**
     * Удаление пользователя из заявки
     * @param dto пользователя в заявке
     * @return обновлённый список пользоватлей заявки
     */
    @Override
    public Optional<List<PassRequestUser>> deleteUserFromPassRequest(PRUserDTO[] dto) {
        Optional<PassRequest> passRequest = getPR(dto[0]);

        // Если заявка существует и является групповой
        if (passRequest.isPresent() && passRequest.get().getType() == PRType.GROUP) {

            // Из списка пользоватлелей заявки выбираются тот,
            // id которого совпадает с искомым id из списка и
            // удаляется если существует.
            for (PRUserDTO userDTO : dto) {
                passRequest.get()
                        .getPassRequestUsers()
                        .stream()
                        .filter(
                                user -> (Objects.equals(
                                        user.getScosId(), userDTO.getUserId())
                                )
                        )
                        .findAny()
                        .ifPresent(
                                user -> passRequestUserRepository.deleteById(user.getId())
                        );
            }
            log.info("user was deleted from pass request");
            return Optional.of(passRequestUserRepository.findAllByPassRequestId(passRequest.get().getId()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Проверка всех заявок на наличие просроченных каждые сутки
     */
    @Scheduled(fixedDelay = 1000*60*60*24)
    private void checkExpiredPassRequests() {
        log.info("checkExpiredPassRequests");
        //System.out.println("checkExpiredPassRequests");
        List<PassRequest> requests = passRequestRepository.findAll();
        requests.stream()
                .filter(request -> isExpired(request.getStatus(), request.getEndDate()))
                .forEach(request -> request.setStatus(PRStatus.EXPIRED));
        passRequestRepository.saveAll(requests);
    }

    /**
     * Получить запросы для университета с поиском и пагинацией по категории
     * @param statuses массив статусов заявок
     * @param requests список заявок, которые пришли в университет
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param search поиск (опционально)
     * @return список заявок по входным параметрам
     */
    private ResponseDTO<PassRequest> getPRByStatusWithPaginationAndSearchForUniversity(
            PRStatus[] statuses,
            List<PassRequest> requests,
            Long page,
            Long pageSize,
            String search) {
        List<PassRequest> requestList = new LinkedList<>();
        for (PRStatus status : statuses) {
            requestList.addAll(
                    requests.stream()
                            .filter(request -> request.getStatus() == status)
                            .collect(Collectors.toList())
            );
        }

        if (Optional.ofNullable(search).isPresent()) {
            requestList = PRUtils.filterRequest(requestList, search, scosAPIService);
        }
        long requestsCount = requestList.size();
        requestList = PRUtils.paginateRequests(requestList, page, pageSize);

        return new ResponseDTO<>(
                page,
                pageSize,
                requestsCount % pageSize == 0 ? requestsCount / pageSize : requestsCount / pageSize + 1,
                requestsCount,
                requestList
        );
    }

    /**
     * Является ли заявка просроченной?
     * @param status статус заявки
     * @param endDate дата конца действия
     * @return ответ true или false
     */
    private boolean isExpired(PRStatus status, LocalDate endDate) {
        return (status != PRStatus.EXPIRED &&
                endDate.isBefore(LocalDate.now()));
    }

    /**
     * Добавить новую запись в список изменений заявки.
     * @param passRequest заявка
     * @param status новый статус
     */
    private void addChangeLogEntryToPassRequest(PassRequest passRequest,
                                                PRStatus status) {
        PassRequestChangeLogEntry entry = new PassRequestChangeLogEntry(
                "PassRequestStatus", passRequest.getStatus().toString(),
                status.toString(), passRequest.getId()
        );
        passRequestChangeLogRepository.save(entry);

        passRequest.setStatus(status);
        passRequestRepository.save(passRequest);
    }

    /**
     * Создать групповую заявку
     * @param dto заявки
     * @param userId идентификатор пользователя
     * @return созданная заявка
     */
    private Optional<PassRequest> createGroupPassRequest(PRDTO dto, String userId) {
        UserDTO author = scosAPIService.getUserDetails(userId).orElseThrow();
        Optional<EmploymentDTO> employment = author.getEmployments()
                .stream()
                .filter(e -> e.getRoles().contains("UNIVERSITY"))
                .findFirst();
        author.setPhoto_url(
                Arrays.stream(
                                scosAPIService.getUserByFIO(
                                        author.getFirst_name(),
                                        author.getLast_name()
                                ).orElseThrow().getData()
                        )
                        .filter(user -> user.getUser_id().equals(author.getUser_id()))
                        .findFirst()
                        .orElseThrow()
                        .getPhoto_url());
        if (employment.isPresent()) {
            Optional<OrganizationDTO> authorOrganization =
                    scosAPIService.getOrganization(
                            employment.get().getOgrn()
                    );


            if (authorOrganization.isPresent()) {
                Optional<String> authorOrganizationGlobalId = authorOrganization.get().getOrganizationId();
                Optional<OrganizationProfileDTO> targetOrganization =
                        scosAPIService.getOrganizationByGlobalId(
                                dto.getTargetUniversityId()
                        );

                if (targetOrganization.isPresent() && authorOrganizationGlobalId.isPresent()) {
                    return Optional.of(new PassRequest(
                            userId,
                            author.getFirst_name(),
                            author.getLast_name(),
                            author.getPatronymic_name(),
                            authorOrganizationGlobalId.get(),
                            authorOrganization.get().getShort_name(),
                            dto.getStartDate(),
                            dto.getEndDate(),
                            dto.getStatus(),
                            dto.getType(),
                            dto.getTargetUniversityAddress(),
                            targetOrganization.get().getShort_name(),
                            dto.getTargetUniversityId(),
                            PRUtils.getRequestNumber(passRequestRepository),
                            author.getPhoto_url())
                    );
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Получить заявку по id
     * @param passRequestDTO заявки
     * @return заявка
     */
    private Optional<PassRequest> getPR(PRDTO passRequestDTO) {
        return PRUtils.getPR(passRequestDTO.getId(), passRequestRepository);
    }

    /**
     * Получить заявку по id
     * @param passRequestUserDTO пользователя заявки
     * @return заявка
     */
    private Optional<PassRequest> getPR(PRUserDTO passRequestUserDTO) {
        return PRUtils.getPR(passRequestUserDTO.getPassRequestId(), passRequestRepository);
    }

    /**
     * Получить заявку по id
     * @param id id заявки
     * @return заявка
     */
    private Optional<PassRequest> getPR(UUID id) {
        return PRUtils.getPR(id, passRequestRepository);
    }
}
