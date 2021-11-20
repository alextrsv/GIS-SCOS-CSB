package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.DynamicQRUser;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestChangeLogEntry;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.comparators.PassRequestCreationDateComparator;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import gisscos.studentcard.entities.enums.RequestsStatusForAdmin;
import gisscos.studentcard.repositories.IDynamicQRUserRepository;
import gisscos.studentcard.repositories.IPassRequestChangeLogRepository;
import gisscos.studentcard.repositories.IPassRequestRepository;
import gisscos.studentcard.repositories.IPassRequestUserRepository;
import gisscos.studentcard.services.IPassRequestService;
import gisscos.studentcard.utils.PassRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final WebClient devScosApiClient;

    @Autowired
    public PassRequestServiceImpl(IPassRequestRepository passRequestRepository,
                                  IPassRequestUserRepository passRequestUserRepository,
                                  IPassRequestChangeLogRepository passRequestChangeLogRepository,
                                  IDynamicQRUserRepository dynamicQRUserRepository,
                                  WebClient devScosApiClient) {
        this.passRequestRepository = passRequestRepository;
        this.passRequestUserRepository = passRequestUserRepository;
        this.passRequestChangeLogRepository = passRequestChangeLogRepository;
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.devScosApiClient = devScosApiClient;
    }

    /**
     * Добавление заявки в БД. Если заявка групповая, со
     * @param dto DTO заявки
     * @return добавленная заявка
     */
    @Override
    public PassRequest addPassRequest(PassRequestDTO dto) {
        // Необходимо сделать так, чтобы одиночные заявки
        // по умолчанию имели бы статус "Отправлена на
        // рассмотрение в целевую ООВО"
        PassRequest passRequest = new PassRequest(
                dto.getUserId(), dto.getTargetUniversityId(),
                dto.getUniversityId(), dto.getStartDate(),
                dto.getEndDate(), dto.getStatus(),
                dto.getType(), dto.getTargetUniversityAddress(),
                dto.getTargetUniversityName(), dto.getUniversityName(),
                getRequestNumber()
        );

//        ADD NEW USER FROM SINGLE REQUEST
        if (dto.getType() == PassRequestType.SINGLE && !dynamicQRUserRepository.existsByUserId(dto.getUserId())){
            dynamicQRUserRepository.save(new DynamicQRUser(dto.getUserId(), dto.getUniversityId()));
        }

        if (dto.getType() == PassRequestType.GROUP) {
            UUID id = passRequestRepository.save(passRequest).getId();

            for (PassRequestUserDTO user : dto.getUsers()) {
                user.setPassRequestId(id);
                addUserToPassRequest(user);
                if (!dynamicQRUserRepository.existsByUserId(user.getUserId()))
                    dynamicQRUserRepository.save(new DynamicQRUser(user.getUserId(), dto.getUniversityId()));
            }
            if (getPassRequestById(id, dto.getUserId()).isPresent())
                return getPassRequestById(id, dto.getUserId()).get();
        }
        log.info("pass request was added");
        return passRequestRepository.save(passRequest);
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
            if (passRequest.get().getUsers().stream().anyMatch(user -> user.getUserId().equals(dto.getUserId()))) {
                log.info("the user is already associated to the pass request");
                return Optional.empty();
            }

            PassRequestUser passRequestUser = new PassRequestUser(
                    dto.getPassRequestId(),
                    dto.getUserId()
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
            if (passRequest.get().getUserId().equals(authorId)) {
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
        List<PassRequest> requestList = passRequestRepository.findAllByUserId(id);

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
                passRequestRepository.findAllByUniversityId(dto.getUniversityId());
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
    public Optional<List<PassRequest>> getPassRequestByStatusForUser(String authorId,
                                                              PassRequestStatus status,
                                                              Long page,
                                                              Long pageSize) {
        List<PassRequest> requests =
                passRequestRepository.findAllByUserId(authorId);
        log.info("Getting passRequests by status");
        return Optional.of(
                requests.stream()
                        .filter(r -> r.getStatus() == status)
                        .sorted(new PassRequestCreationDateComparator())
                        .skip(pageSize * (page - 1))
                        .limit(pageSize)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Получить количество заявок по статусу для пользователя
     * @param authorId идентификатор пользователя
     * @return мапа с парами ключ - значение, где ключ - статус, значение - количество заявок
     */
    @Override
    public Optional<Map<PassRequestStatus, Long>> getPassRequestCountByStatusForUser(String authorId) {
        List<PassRequest> requests =
                passRequestRepository.findAllByUserId(authorId);
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

            return Optional.of(request.get().getUsers());
        }

        log.warn("Pass request with id {} not found", dto.getId());
        return Optional.empty();
    }

    @Override
    public Optional<List<PassRequest>> getPassRequestsForAdmin(
            RequestsStatusForAdmin status,
            String targetUniversityId,
            Long page,
            Optional<String> search
    ) {
        switch (status) {
            case PROCESSED:
                return getProcessedPassRequests(targetUniversityId, page, search);
            case IN_PROCESSING:
                return getPassRequestsInProcessing(targetUniversityId, page, search);
            case FOR_PROCESSING:
                return getPassRequestsForProcessing(targetUniversityId, page, search);
            default:
                return Optional.empty();
        }
    }

    @Override
    public Optional<List<PassRequest>> getPassRequestCountByStatusForAdmin(String authorId, PassRequestStatus status) {
        return Optional.empty();
    }

    /**
     * Получение заявок для обработки.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @return список заявок для обработки
     */
    public Optional<List<PassRequest>> getPassRequestsForProcessing(
            String universityId,
            Long page,
            Optional<String> search) {
        log.info("collect requests sent for consideration to the target OOVO");
        return search.map(s -> filterRequest(getPassRequestByStatusForUniversity(
                PassRequestStatus.TARGET_ORGANIZATION_REVIEW,
                universityId
        ), s)
                .stream()
                .skip(5L * (page - 1))
                .limit(5)
                .collect(Collectors.toList())).or(() -> Optional.of(
                getPassRequestByStatusForUniversity(
                        PassRequestStatus.TARGET_ORGANIZATION_REVIEW,
                        universityId
                )
                        .stream()
                        .skip(5L * (page - 1))
                        .limit(5)
                        .collect(Collectors.toList())
        ));
    }

    /**
     * Получение заявок в обработке.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @return список заявок в обработке
     */
    public Optional<List<PassRequest>> getPassRequestsInProcessing(
            String universityId,
            Long page,
            Optional<String> search) {
        log.info("collect requests sent in consideration to the target OOVO");
        return search.map(s -> filterRequest(getPassRequestByStatusForUniversity(
                PassRequestStatus.PROCESSED_IN_TARGET_ORGANIZATION,
                universityId
        ), s)
                .stream()
                .skip(5L * (page - 1))
                .limit(5)
                .collect(Collectors.toList())).or(() -> Optional.of(
                getPassRequestByStatusForUniversity(
                        PassRequestStatus.PROCESSED_IN_TARGET_ORGANIZATION,
                        universityId
                )
                        .stream()
                        .skip(5L * (page - 1))
                        .limit(5)
                        .collect(Collectors.toList())
        ));
    }

    /**
     * Получение обработанных заявок.
     * @param universityId идентификатор ООВО
     * @param page номер страницы
     * @return список обработанных заявок
     */
    public Optional<List<PassRequest>> getProcessedPassRequests(
            String universityId,
            Long page,
            Optional<String> search) {

        List<PassRequest> requestList = getPassRequestByStatusForUniversity(
                        PassRequestStatus.ACCEPTED,
                        universityId
        );

        requestList.addAll(getPassRequestByStatusForUniversity(
                        PassRequestStatus.EXPIRED,
                        universityId
                ));

        requestList.addAll(getPassRequestByStatusForUniversity(
                        PassRequestStatus.REJECTED_BY_TARGET_ORGANIZATION,
                        universityId
                ));

        requestList.addAll(getPassRequestByStatusForUniversity(
                        PassRequestStatus.CANCELED_BY_CREATOR,
                        universityId
                ));

        log.info("collect considered requests sent for to the OOVO");
        return search.map(s -> filterRequest(requestList, s)
                .stream()
                .skip(5L * (page - 1))
                .limit(5)
                .collect(Collectors.toList())).or(() -> Optional.of(
                requestList
                        .stream()
                        .skip(5L * (page - 1))
                        .limit(5)
                        .collect(Collectors.toList())
        ));
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
            passRequest.get().setUserId(dto.getUserId());
            passRequest.get().setEndDate(dto.getEndDate());
            passRequest.get().setStartDate(dto.getStartDate());
            passRequest.get().setUniversityId(dto.getUniversityId());
            passRequest.get().setTargetUniversityId(dto.getTargetUniversityId());
            passRequest.get().setTargetUniversityAddress(dto.getTargetUniversityAddress());
            passRequest.get().setTargetUniversityName(dto.getTargetUniversityName());
            passRequest.get().setUniversityName(dto.getUniversityName());
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
                        .getUsers()
                        .stream()
                        .filter(
                                user -> (Objects.equals(
                                        user.getUserId(), userDTO.getUserId())
                                )
                        )
                        .findAny()
                        .ifPresent(
                                user -> passRequestUserRepository.deleteById(user.getId())
                        );
            }
            log.info("user list with has been deleted from pass request");
            // Удалённый пользователь (по сути PassRequestUserDTO)
            return Optional.of(passRequest.get().getUsers());
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
        return (Objects.equals(request.getUserId(), dto.getUserId()));
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

    private List<PassRequest> filterRequest(List<PassRequest> requests,
                                            String search) {
        switch (PassRequestUtils.getFilterType(search)) {
            case ORGANIZATION:
                return PassRequestUtils
                        .filterRequestListByOrganizations(
                                requests,
                                search,
                                devScosApiClient
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

    private Long getRequestNumber() {
        return passRequestRepository.countAllByNumberGreaterThan(0L) + 1;
    }
}
