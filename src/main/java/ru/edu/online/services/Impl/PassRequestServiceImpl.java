package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestChangeLogEntry;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.comparators.PassRequestCreationDateComparator;
import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.PassRequestType;
import ru.edu.online.entities.enums.RequestsStatusForAdmin;
import ru.edu.online.repositories.IDynamicQRUserRepository;
import ru.edu.online.repositories.IPassRequestChangeLogRepository;
import ru.edu.online.repositories.IPassRequestRepository;
import ru.edu.online.repositories.IPassRequestUserRepository;
import ru.edu.online.services.IPassRequestCommentsService;
import ru.edu.online.services.IPassRequestService;
import ru.edu.online.utils.PassRequestUtils;
import ru.edu.online.utils.ScosApiUtils;
import ru.edu.online.utils.VamApiUtils;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с заявками.
 */
@Service
@Slf4j
public class PassRequestServiceImpl implements IPassRequestService {

    private final IPassRequestRepository passRequestRepository;
    private final IPassRequestUserRepository passRequestUserRepository;
    private final IPassRequestChangeLogRepository passRequestChangeLogRepository;
    private final IDynamicQRUserRepository dynamicQRUserRepository;

    private final IPassRequestCommentsService passRequestCommentsService;

    private final WebClient devScosApiClient;
    private final WebClient devVamApiClient;

    @Autowired
    public PassRequestServiceImpl(IPassRequestRepository passRequestRepository,
                                  IPassRequestUserRepository passRequestUserRepository,
                                  IPassRequestChangeLogRepository passRequestChangeLogRepository,
                                  IDynamicQRUserRepository dynamicQRUserRepository,
                                  IPassRequestCommentsService passRequestCommentsService,
                                  WebClient devScosApiClient,
                                  WebClient devVamApiClient) {
        this.passRequestRepository = passRequestRepository;
        this.passRequestUserRepository = passRequestUserRepository;
        this.passRequestChangeLogRepository = passRequestChangeLogRepository;
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.passRequestCommentsService = passRequestCommentsService;
        this.devScosApiClient = devScosApiClient;
        this.devVamApiClient = devVamApiClient;
    }

    /**
     * Добавление одиночной заявки в БД.
     * @param dto DTO заявки
     * @return добавленная заявка
     */
    @Override
    public Optional<PassRequest> addSinglePassRequest(PassRequestDTO dto, Principal principal) {
        Optional<PassRequest> passRequest = createSinglePassRequest(dto, principal);

        if (passRequest.isPresent()) {
            //ADD NEW USER FROM SINGLE REQUEST
            if (!dynamicQRUserRepository.existsByUserId(dto.getAuthorId())){
                dynamicQRUserRepository.save(new DynamicQRUser(dto.getAuthorId(), dto.getUniversityId()));
            }

            UUID passRequestId = passRequestRepository.save(passRequest.get()).getId();
            passRequestCommentsService.addCommentToPassRequest(
                    new PassRequestCommentDTO(
                            principal.getName(),
                            passRequestId,
                            dto.getComment()
                    )
            );
            log.info("single pass request was added");
            return getPassRequest(passRequestId);
        }

        return Optional.empty();
    }

    /**
     * Добавление групповой заявки в БД.
     * @param dto DTO заявки
     * @return добавленная заявка
     */
    @Override
    public Optional<PassRequest> addGroupPassRequest(PassRequestDTO dto, Principal principal) {
        Optional<PassRequest> passRequest = createGroupPassRequest(dto, principal);

        if (passRequest.isPresent()) {
            UUID passRequestId = passRequestRepository.save(passRequest.get()).getId();

            for (PassRequestUserDTO user : dto.getUsers()) {
                user.setPassRequestId(passRequestId);
                addUserToPassRequest(user);
                if (!dynamicQRUserRepository.existsByUserId(user.getUserId()))
                    dynamicQRUserRepository.save(new DynamicQRUser(user.getUserId(), dto.getUniversityId()));
            }
            if (getPassRequestById(passRequestId, dto.getAuthorId()).isPresent())
                return getPassRequestById(passRequestId, principal.getName());
            log.info("group pass request was added");

            passRequestCommentsService.addCommentToPassRequest(
                    new PassRequestCommentDTO(
                            principal.getName(),
                            passRequestId,
                            dto.getComment()
                    )
            );
            return getPassRequest(passRequestId);
        }

        return Optional.empty();
    }

    /**
     * Добавление пользователя в список заявки
     * @param dto dto пользователя заявки
     * @return список всех пользователей, находящихся в заявке
     */
    @Override
    public Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO dto) {
        Optional<PassRequest> passRequest = getPassRequest(dto);

        // Если есть такая заявка и она является групповой
        if (passRequest.isPresent() && passRequest.get().getType() == PassRequestType.GROUP) {
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
                    dto.getPatronymicName()
            );
            passRequestUserRepository.save(passRequestUser);
            log.info("the user was associated to the pass request successfully");
            return Optional.of(passRequestUserRepository.findAllByPassRequestId(passRequest.get().getId()));
        } else
            log.info("nothing to associate, the pass request type isn't \"GROUP\"");
            return Optional.empty();
    }

    /**
     * Получение заявки по id
     * @param passRequestId заявки
     * @return заявка
     */
    @Override
    public Optional<PassRequest> getPassRequestById(UUID passRequestId, String authorId) {
        Optional<PassRequest> passRequest = getPassRequest(passRequestId);
        if (passRequest.isPresent()) {
            if (passRequest.get().getAuthorId().equals(authorId)) {
                log.info("getting pass request by id: {}", passRequestId);
                return passRequest;
            }
        }
        log.warn("User with {} is not author of request", authorId);
        return Optional.empty();
    }

    /**
     * Получить список заявок по id пользователя
     * @param id пользователя
     * @return список заявок
     */
    @Override
    public Optional<List<PassRequest>> getPassRequestsByUserId(String id) {
        log.info("getting pass request by user id: {}", id);
        List<PassRequest> requestList = passRequestRepository.findAllByAuthorId(id);

        return Optional.of(requestList);
    }

    /**
     * Получить список заявок по статусу для университета
     * @param dto заявки
     * @param page номер страницы
     * @param pageSize размер страницы
     * @return список заявок с определенным статусом
     */
    @Override
    public Optional<List<PassRequest>> getPassRequestByStatusForUniversity(PassRequestDTO dto,
                                                                           Long page,
                                                                           Long pageSize) {
        List<PassRequest> requests =
                passRequestRepository.findAllByAuthorUniversityId(dto.getUniversityId());
        log.info("Getting passRequests by status");
        return Optional.of(
                requests.stream()
                        .filter(r -> r.getStatus() == dto.getStatus())
                        .sorted(new PassRequestCreationDateComparator())
                        .skip(pageSize * (page - 1))
                        .limit(pageSize)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Получить список заявок по статусу для пользователя
     * @param authorId заявки
     * @param page номер страницы
     * @param pageSize размер страницы
     * @return список заявок с определенным статусом
     */
    @Override
    public Optional<PassRequestsResponseDTO> getPassRequestByStatusForUser(String authorId,
                                                                     String status,
                                                                     Long page,
                                                                     Long pageSize) {
        List<PassRequest> requests =
                passRequestRepository.findAllByAuthorId(authorId);
        log.info("Getting passRequests by status");
        switch (status) {
            case "accepted":
                return Optional.of(aggregatePassRequestsByStatusWithPaginationForUser(
                        requests,
                        new PassRequestStatus[]{PassRequestStatus.ACCEPTED},
                        page,
                        pageSize
                ));
            case "rejected":
                return Optional.of(aggregatePassRequestsByStatusWithPaginationForUser(
                        requests,
                        new PassRequestStatus[]{
                                PassRequestStatus.REJECTED_BY_TARGET_ORGANIZATION,
                                PassRequestStatus.CANCELED_BY_CREATOR
                        },
                        page,
                        pageSize
                ));
            case "processing":
                return Optional.of(aggregatePassRequestsByStatusWithPaginationForUser(
                        requests,
                        new PassRequestStatus[]{
                                PassRequestStatus.TARGET_ORGANIZATION_REVIEW,
                                PassRequestStatus.PROCESSED_IN_TARGET_ORGANIZATION
                        },
                        page,
                        pageSize
                ));
            case "expired":
                return Optional.of(aggregatePassRequestsByStatusWithPaginationForUser(
                        requests,
                        new PassRequestStatus[]{PassRequestStatus.EXPIRED},
                        page,
                        pageSize
                ));
            default:
                return Optional.empty();
        }
    }

    /**
     * Получить количество заявок по статусу для пользователя
     * @param authorId идентификатор пользователя
     * @return мапа с парами ключ - значение, где ключ - статус, значение - количество заявок
     */
    @Override
    public Optional<Map<PassRequestStatus, Long>> getPassRequestCountByStatusForUser(String authorId) {
        List<PassRequest> requests =
                passRequestRepository.findAllByAuthorId(authorId);
        log.info("Getting passRequests count by status for user");
        Map<PassRequestStatus, Long> statusesCount = new HashMap<>();
        for (PassRequestStatus status : PassRequestStatus.values()) {
            statusesCount.put(
                            status,
                            requests.stream()
                                    .filter(r -> r.getStatus().equals(status))
                                    .count()
            );
        }
        return Optional.of(statusesCount);
    }

    /**
     * Получить список пользователей групповой заявки.
     * @param dto заявки
     * @return список пользователей заявки или Optional.empty
     * если заявка одиночная или вообще не найдена.
     */
    @Override
    public Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto) {
        Optional<PassRequest> request = getPassRequest(dto);

        if (request.isPresent()) {
            if (request.get().getType() == PassRequestType.SINGLE) {
                log.warn("Pass request with {} id has single type.",
                        request.get().getId());

                return Optional.empty();
            }
            log.info("Getting users from pass request with id {}",
                    request.get().getId());

            return Optional.of(request.get().getPassRequestUsers());
        }

        log.warn("Pass request with id {} not found", dto.getId());
        return Optional.empty();
    }

    /**
     * Получить заявки для администратора
     * @param status стутус заявок
     * @param page номер страницы (по умолчанию по 5 заявок для админа)
     * @param search поиск по заявкам. Возможен по названию ООВО и номеру заявки
     * @return отобранные по критериям заявки
     */
    @Override
    public Optional<PassRequestsResponseDTO> getPassRequestsForAdmin(RequestsStatusForAdmin status,
                                                               Long page,
                                                               Long pageSize,
                                                               Optional<String> search,
                                                               Principal principal
    ) {
        UserDTO admin = ScosApiUtils.getUserDetails(devScosApiClient, principal);
        String adminUniversityId = admin
                .getEmployments()
                .stream()
                .filter(e -> e.getRoles().contains("UNIVERSITY"))
                .findFirst()
                .get()
                .getOgrn();

        switch (status) {
            case PROCESSED:
                return Optional.of(getProcessedPassRequests(adminUniversityId, page, pageSize, search));
            case IN_PROCESSING:
                return Optional.of(getPassRequestsInProcessing(adminUniversityId, page, pageSize, search));
            case FOR_PROCESSING:
                return Optional.of(getPassRequestsForProcessing(adminUniversityId, page, pageSize, search));
            case EXPIRED:
                return Optional.of(getExpiredPassRequests(adminUniversityId, page, pageSize, search));
            default:
                return Optional.empty();
        }
    }

    /**
     * Получение заявок для обработки.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param search поиск
     * @return список заявок в обработке
     */
    public PassRequestsResponseDTO getPassRequestsForProcessing(
            String universityId,
            Long page,
            Long pageSize,
            Optional<String> search) {
        log.info("collect requests sent for consideration to the target OOVO");
        return aggregatePassRequestsByStatusWithPaginationAndSearchForUniversity(
                new PassRequestStatus[]{PassRequestStatus.TARGET_ORGANIZATION_REVIEW},
                universityId,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение заявок в обработке.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param search поиск
     * @return список заявок в обработке
     */
    public PassRequestsResponseDTO getPassRequestsInProcessing(
            String universityId,
            Long page,
            Long pageSize,
            Optional<String> search) {
        log.info("collect requests sent in consideration to the target OOVO");

        return aggregatePassRequestsByStatusWithPaginationAndSearchForUniversity(
                new PassRequestStatus[]{PassRequestStatus.PROCESSED_IN_TARGET_ORGANIZATION},
                universityId,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение просроченных заявок.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param search поиск
     * @return список обработанных заявок
     */
    public PassRequestsResponseDTO getExpiredPassRequests(String universityId,
                                                          Long page,
                                                          Long pageSize,
                                                          Optional<String> search) {
        log.info("collect expired requests sent for to the OOVO");
        return aggregatePassRequestsByStatusWithPaginationAndSearchForUniversity(
                new PassRequestStatus[]{PassRequestStatus.EXPIRED},
                universityId,
                page,
                pageSize,
                search
        );
    }

    /**
     * Получение обработанных заявок.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param search поиск
     * @return список обработанных заявок
     */
    public PassRequestsResponseDTO getProcessedPassRequests(String universityId,
                                                            Long page,
                                                            Long pageSize,
                                                            Optional<String> search) {

        log.info("collect considered requests sent for to the OOVO");
        return aggregatePassRequestsByStatusWithPaginationAndSearchForUniversity(
                new PassRequestStatus[]{
                        PassRequestStatus.ACCEPTED,
                        PassRequestStatus.REJECTED_BY_TARGET_ORGANIZATION,
                        PassRequestStatus.CANCELED_BY_CREATOR
                },
                universityId,
                page,
                pageSize,
                search
        );
    }

    /**
     * Обновление заявки
     * @param dto DTO обновленной заявки
     * @return обновленная заявка
     */
    @Override
    public Optional<PassRequest> updatePassRequest(PassRequestDTO dto) {
        Optional<PassRequest> passRequest = getPassRequest(dto);

        if (passRequest.isPresent()) {
            passRequest.get().setType(dto.getType());
            passRequest.get().setStatus(dto.getStatus());
            passRequest.get().setAuthorId(dto.getAuthorId());
            passRequest.get().setEndDate(dto.getEndDate());
            passRequest.get().setStartDate(dto.getStartDate());
            passRequest.get().setTargetUniversityId(dto.getTargetUniversityId());
            passRequest.get().setTargetUniversityAddress(dto.getTargetUniversityAddress());
            passRequest.get().setTargetUniversityName(dto.getTargetUniversityName());
            passRequestRepository.save(passRequest.get());

            log.info("pass request with id: {} was updated", dto.getId());
            return passRequest;
        } else
            log.warn("pass request with id: {} not found", dto.getId());
            return Optional.empty();
    }

    /**
     * Обновление статуса заявки
     * @param dto DTO обновленной заявки
     * @return обновленная заявка
     */
    @Override
    public Optional<PassRequest> updatePassRequestStatus(PassRequestDTO dto) {
        Optional<PassRequest> passRequest = getPassRequest(dto);

        if (passRequest.isPresent()) {
            PassRequestChangeLogEntry entry = new PassRequestChangeLogEntry(
                    "PassRequestStatus", passRequest.get().getStatus().toString(),
                    dto.getStatus().toString(), dto.getId()
            );
            passRequestChangeLogRepository.save(entry);

            passRequest.get().setStatus(dto.getStatus());
            passRequestRepository.save(passRequest.get());

            log.info("pass request with id: {} was updated", dto.getId());
            return passRequest;
        } else
            log.warn("pass request with id: {} not found", dto.getId());
            return Optional.empty();
    }

    /**
     * Отменить заявку
     * @param dto создателя заявки
     * @return отменённая заявка или Optional.empty
     * если заявка не найдена, пользователь не является автором
     * или заявка уже не находится на рассмотрении (несоответствие статуса)
     */
    @Override
    public Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto) {
        Optional<PassRequest> request = getPassRequest(dto);

        if (request.isPresent()) {
            // Являестя ли пользователь создателем заявки?
            // если да, получаем статус, нет - null
            PassRequestStatus status =
                    isAuthor(request.get(), dto) ? request.get().getStatus() : null;
            // Если заявку ещё имеет смысл отменять.
            // (с остальными статусами не актуально)
            if (status == PassRequestStatus.TARGET_ORGANIZATION_REVIEW) {

                request.get().setStatus(PassRequestStatus.CANCELED_BY_CREATOR);
                passRequestRepository.save(request.get());
                log.info("Pass request cancelled by creator");
                return request;
            }
            log.warn("Impossible to cancel pass request! " +
                    "UserDTO is not author or pass request status invalid.");
            return Optional.empty();
        }
        log.warn("Impossible to cancel pass request! Pass request not found.");
        return Optional.empty();
    }

    /**
     * Удаление заявки по id
     * @param id заявки
     * @return в случае, если заявка была найдена и удалена,
     * возвращается она, если нет - Optional.empty()
     */
    @Override
    public Optional<PassRequest> deletePassRequestById(UUID id) {
        Optional<PassRequest> passRequest = getPassRequest(id);
        if (passRequest.isPresent()) {
            passRequestRepository.deleteById(id);
            log.info("pass request with id: {} was deleted", id);
            return passRequest;
        }
        log.info("pass request with id: {} wasn't found", id);
        return Optional.empty();
    }

    /**
     * Удаление пользователя из заявки
     * @param dto пользователя в заявке
     * @return удаленный из заявки пользователь если таковой найден
     */
    @Override
    public Optional<List<PassRequestUser>> deleteUserFromPassRequest(PassRequestUserDTO[] dto) {
        Optional<PassRequest> passRequest = getPassRequest(dto[0]);

        // Если заявка существует и является групповой
        if (passRequest.isPresent() && passRequest.get().getType() == PassRequestType.GROUP) {

            // Из списка пользоватлелей заявки выбираются тот,
            // id которого совпадает с искомым id из списка и
            // удаляется если существует.
            for (PassRequestUserDTO userDTO : dto) {
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
            log.info("user list with has been deleted from pass request");
            // Удалённый пользователь (по сути PassRequestUserDTO)
            return Optional.of(passRequest.get().getPassRequestUsers());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Удаление просроченных заявок
     * @return список удаленных заявок
     */
    @Override
    public Optional<List<PassRequest>> getExpiredPassRequests() {
        // Поиск просроченных заявок, изменение их статуса на EXPIRED
        checkExpiredPassRequests();

        // Поиск заявок со статусом EXPIRED и CANCELED_BY_CREATOR
        List<PassRequest> expiredList =
                passRequestRepository.findAllByStatus(PassRequestStatus.EXPIRED);
        /* Функциональность временно ограничена
        expiredList
                .addAll(
                        passRequestRepository.findAllByStatus(PassRequestStatus.CANCELED_BY_CREATOR)
                );

        for (PassRequest request : expiredList) {
            passRequestRepository.deleteById(request.getId());
        }
        log.info("expired pass requests has been deleted");
        */
        log.info("expired pass requests has been get");
        return Optional.of(expiredList);
    }

    /**
     * Проверка всех заявок на наличие просроченных
     */
    private void checkExpiredPassRequests() {
        List<PassRequest> requests = passRequestRepository.findAll();
        requests.stream()
                .filter(request -> isExpired(request.getStatus(), request.getStartDate()))
                .forEach(request -> request.setStatus(PassRequestStatus.EXPIRED));
        passRequestRepository.saveAll(requests);
    }

    /**
     * Является ли заявка просроченной?
     * @param status статус заявки
     * @param startDate дата начала действия
     * @return ответ true или false
     */
    private boolean isExpired(PassRequestStatus status, LocalDate startDate) {
        return (status != PassRequestStatus.ACCEPTED &&
                status != PassRequestStatus.EXPIRED &&
                status != PassRequestStatus.CANCELED_BY_CREATOR &&
                startDate.isBefore(LocalDate.now()));
    }

    /**
     * Является ли пользователь автором заявки?
     * @param request заявка
     * @param dto пользователя заявки
     * @return true - является, false - не является
     */
    private boolean isAuthor(PassRequest request, PassRequestUserDTO dto) {
        return (Objects.equals(request.getAuthorId(), dto.getUserId()));
    }

    /**
     * Получить заявку по id
     * @param dto заявки
     * @return заявка
     */
    private Optional<PassRequest> getPassRequest(PassRequestDTO dto) {
        return passRequestRepository.findById(dto.getId());
    }

    /**
     * Получить заявку по id
     * @param dto пользователя заявки
     * @return заявка
     */
    private Optional<PassRequest> getPassRequest(PassRequestUserDTO dto) {
        return passRequestRepository.findById(dto.getPassRequestId());
    }

    /**
     * Получить азявку по id
     * @param id id заявки
     * @return заявка
     */
    private Optional<PassRequest> getPassRequest(UUID id) {
        return passRequestRepository.findById(id);
    }

    /**
     * Создать одиночную заявку
     * @param dto заявки
     * @param principal авторизация
     * @return созданная заявка
     */
    private Optional<PassRequest> createSinglePassRequest(PassRequestDTO dto, Principal principal) {
        UserDTO author = getUserInfo(principal);
        Optional<StudentDTO> student =
                VamApiUtils.getStudents("email", author.getEmail(), devVamApiClient)
                        .getResults()
                        .stream()
                        .filter(s -> s.getStudy_year() != null)
                        .findFirst();

        if (student.isPresent()) {
            Optional<OrganizationDTO> authorOrganization =
                    ScosApiUtils.getOrganizationByGlobalId(
                            devScosApiClient,
                            student.get().getOrganization_id()
                    );
            if (authorOrganization.isPresent()) {
                Optional<OrganizationDTO> targetOrganization =
                        ScosApiUtils.getOrganization(
                                devScosApiClient,
                                dto.getTargetUniversityId()
                        );

                if (targetOrganization.isPresent()) {
                    return Optional.of(new PassRequest(
                            principal.getName(),
                            author.getFirst_name(),
                            author.getLast_name(),
                            author.getPatronymic_name(),
                            student.get().getOrganization_id(),
                            authorOrganization.get().getShort_name(),
                            dto.getStartDate(),
                            dto.getEndDate(),
                            dto.getStatus(),
                            dto.getType(),
                            dto.getTargetUniversityAddress(),
                            targetOrganization.get().getShort_name(),
                            dto.getTargetUniversityId(),
                            getRequestNumber())
                    );
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Создать групповую заявку
     * @param dto заявки
     * @param principal авторизация
     * @return созданная заявка
     */
    private Optional<PassRequest> createGroupPassRequest(PassRequestDTO dto, Principal principal) {
        UserDTO author = getUserInfo(principal);
        String authorOrganizationOGRN = author.getEmployments()
                .stream()
                .filter(e -> e.getRoles().contains("UNIVERSITY"))
                .findFirst()
                .get()
                .getOgrn();
        Optional<OrganizationDTO> authorOrganization =
                ScosApiUtils.getOrganization(
                        devScosApiClient,
                        authorOrganizationOGRN
                );
        if (authorOrganization.isPresent()) {
            Optional<OrganizationDTO> targetOrganization =
                    ScosApiUtils.getOrganization(
                            devScosApiClient,
                            dto.getTargetUniversityId()
                    );

            if (targetOrganization.isPresent()) {
                return Optional.of(new PassRequest(
                        principal.getName(),
                        author.getFirst_name(),
                        author.getLast_name(),
                        author.getPatronymic_name(),
                        authorOrganizationOGRN,
                        authorOrganization.get().getShort_name(),
                        dto.getStartDate(),
                        dto.getEndDate(),
                        dto.getStatus(),
                        dto.getType(),
                        dto.getTargetUniversityAddress(),
                        targetOrganization.get().getShort_name(),
                        dto.getTargetUniversityId(),
                        getRequestNumber())
                );
            }
        }

        return Optional.empty();
    }

    private UserDTO getUserInfo(Principal principal) {
        return ScosApiUtils.getUserDetails(devScosApiClient, principal);
    }

    /**
     * Получить список заявок для университета по статусу
     * @param status статус заявки
     * @param universityId идентификатор университета
     * @return список заявок
     */
    private List<PassRequest> getPassRequestByStatusForUniversity(PassRequestStatus status,
                                                                  String universityId) {
        return passRequestRepository
                .findAllByTargetUniversityIdAndStatus(
                        universityId,
                        status
                );
    }

    /**
     * Получить список заявок по id создателя и статусу
     * @param requests список заявок
     * @param statuses стутус заявки
     * @param page номер страницы
     * @param pageSize размер страницы     *
     * @return список отобранных заявок по критериям выше
     */
    private PassRequestsResponseDTO aggregatePassRequestsByStatusWithPaginationForUser(List<PassRequest> requests,
                                                                                       PassRequestStatus[] statuses,
                                                                                       Long page,
                                                                                       Long pageSize) {
        List<PassRequest> filteredRequest = new LinkedList<>();
        for (PassRequestStatus status : statuses) {
            filteredRequest.addAll(
                    requests.stream()
                            .filter(request -> request.getStatus() == status)
                            .collect(Collectors.toList())
            );
        }
        return new PassRequestsResponseDTO(
                page,
                pageSize,
                filteredRequest.size() / pageSize,
                (long) filteredRequest.size(),
                paginateRequests(filteredRequest, page, pageSize)
        );
    }


    /**
     * Получить запросы для университета с поиском и пагинацией по категории
     * @param statuses массив статусов заявок
     * @param universityId идентификатор университета
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param search поиск (опционально)
     * @return список заявок по входным параметрам
     */
    private PassRequestsResponseDTO aggregatePassRequestsByStatusWithPaginationAndSearchForUniversity(
            PassRequestStatus[] statuses,
            String universityId,
            Long page,
            Long pageSize,
            Optional<String> search) {
        List<PassRequest> requestList = new LinkedList<>();
        for (PassRequestStatus status : statuses) {
            requestList.addAll(
                    getPassRequestByStatusForUniversity(
                            status,
                            universityId
                    )
            );
        }

        if (search.isPresent()) {
            requestList = PassRequestUtils.filterRequest(requestList, search.get(), devScosApiClient);
        }
        long requestsCount = requestList.size();
        requestList = paginateRequests(requestList, page, pageSize);

        return new PassRequestsResponseDTO(
                page,
                pageSize,
                requestsCount / pageSize,
                requestsCount,
                requestList
        );
    }

    private List<PassRequest> paginateRequests(List<PassRequest> requests, long page, long pageSize) {
        return requests
                .stream()
                .sorted(new PassRequestCreationDateComparator())
                .skip(pageSize * (page - 1))
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    private Long getRequestNumber() {
        return passRequestRepository.countAllByNumberGreaterThan(0L) + 1;
    }
}
