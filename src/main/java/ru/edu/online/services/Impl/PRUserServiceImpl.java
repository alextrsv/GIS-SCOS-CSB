package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.entities.DynamicQRUser;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.*;
import ru.edu.online.entities.enums.PRStatus;
import ru.edu.online.entities.enums.PRType;
import ru.edu.online.repositories.IDynamicQRUserRepository;
import ru.edu.online.repositories.IPRRepository;
import ru.edu.online.repositories.IPRUserRepository;
import ru.edu.online.services.IPRCommentsService;
import ru.edu.online.services.IPRUserService;
import ru.edu.online.services.IScosAPIService;
import ru.edu.online.services.IVamAPIService;
import ru.edu.online.utils.PRUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис заявок пользователя.
 * Описывает логику работы заявок для студента.
 */
@Slf4j
@Service
public class PRUserServiceImpl implements IPRUserService {

    /** Репозиторий пользователей заявок */
    private final IPRUserRepository passRequestUserRepository;
    /** Репозиторий динамических QR - кодов */
    private final IDynamicQRUserRepository dynamicQRUserRepository;
    /** Репозиторий заявок */
    private final IPRRepository passRequestRepository;

    /** Сервис комментариев заявок */
    private final IPRCommentsService passRequestCommentsService;
    /** Сервис для работы с АПИ СЦОСа */
    private final IScosAPIService scosAPIService;
    /** Сервис для работы с АПИ ВАМа */
    private final IVamAPIService vamAPIService;

    @Autowired
    public PRUserServiceImpl(IPRUserRepository passRequestUserRepository,
                             IDynamicQRUserRepository dynamicQRUserRepository,
                             IPRRepository passRequestRepository,
                             IPRCommentsService passRequestCommentsService,
                             IScosAPIService scosAPIService,
                             IVamAPIService vamAPIService) {

        this.passRequestUserRepository = passRequestUserRepository;
        this.dynamicQRUserRepository = dynamicQRUserRepository;
        this.passRequestRepository = passRequestRepository;
        this.passRequestCommentsService = passRequestCommentsService;
        this.scosAPIService = scosAPIService;
        this.vamAPIService = vamAPIService;
    }

    /**
     * Добавление одиночной заявки в БД.
     * @param dto DTO заявки
     * @return добавленная заявка
     */
    @Override
    public Optional<PassRequest> addSinglePassRequest(PRDTO dto, String userId) {
        Optional<PassRequest> passRequest = createSinglePassRequest(dto, userId);

        if (passRequest.isPresent()) {
            //ADD NEW USER FROM SINGLE REQUEST
            if (!dynamicQRUserRepository.existsByUserId(dto.getAuthorId())){
                dynamicQRUserRepository.save(new DynamicQRUser(passRequest.get().getAuthorId(), passRequest.get().getAuthorUniversityId()));
            }

            UUID passRequestId = passRequestRepository.save(passRequest.get()).getId();
            passRequestCommentsService.addCommentToPassRequest(
                    new PRCommentDTO(
                            userId,
                            passRequestId,
                            dto.getComment()
                    )
            );
            log.info("single pass request was added");
            dto.setId(passRequestId);
            //updatePassRequestStatus(dto);
            return getPR(passRequestId);
        }

        return Optional.empty();
    }

    /**
     * Получение заявки по id
     * @param passRequestId заявки
     * @return заявка
     */
    @Override
    public Optional<PassRequest> getPassRequestById(UUID passRequestId) {
        Optional<PassRequest> passRequest = getPR(passRequestId);
        if (passRequest.isPresent()) {
            log.info("getting pass request with id: {}", passRequestId);
            return passRequest;
        }
        log.warn("pass request with id: {}\nnot found!", passRequestId);
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
     * Получение всех одобренных заявок пользователя (доступов)
     * @param authorId идентификатор пользователя
     * @return список одобренных заявок
     */
    @Override
    public Optional<GenericResponseDTO<PassRequest>> getAcceptedPassRequests(String authorId) {
        log.info("getting accepted passRequests for user with id {}", authorId);
        List<PassRequest> requests =
                passRequestRepository.findAllByAuthorId(authorId);
        // Добавление всех групповых заявок, в которых фигурирует пользователь
        requests.addAll(passRequestUserRepository.getByScosId(authorId)
                .stream()
                .map(PassRequestUser::getPassRequestId)
                .map(this::getPassRequestById)
                .map(Optional::get)
                .collect(Collectors.toList()));

        return Optional.of(getPRByStatusWithPaginationForUser(
                requests,
                new PRStatus[]{PRStatus.ACCEPTED},
                (long) 1,
                (long) requests.size()
        ));
    }

    /**
     * Получить список заявок по статусу для пользователя
     * @param authorId идентификатор автора заявки
     * @param page номер страницы
     * @param pageSize размер страницы
     * @return список заявок с определенным статусом
     */
    @Override
    public Optional<GenericResponseDTO<PassRequest>> getPassRequestByStatusForUser(String authorId,
                                                                                   String status,
                                                                                   Long page,
                                                                                   Long pageSize) {
        List<PassRequest> requests = getUserRequests(authorId);
        log.info("Getting passRequests by status");
        switch (status) {
            case "accepted":
                return Optional.of(getPRByStatusWithPaginationForUser(
                        requests,
                        new PRStatus[]{PRStatus.ACCEPTED},
                        page,
                        pageSize
                ));
            case "rejected":
                return Optional.of(getPRByStatusWithPaginationForUser(
                        requests,
                        new PRStatus[]{PRStatus.REJECTED_BY_TARGET_ORGANIZATION},
                        page,
                        pageSize
                ));
            case "processing":
                return Optional.of(getPRByStatusWithPaginationForUser(
                        requests,
                        new PRStatus[]{
                                PRStatus.TARGET_ORGANIZATION_REVIEW,
                                PRStatus.PROCESSED_IN_TARGET_ORGANIZATION
                        },
                        page,
                        pageSize
                ));
            case "expired":
                return Optional.of(getPRByStatusWithPaginationForUser(
                        requests,
                        new PRStatus[]{PRStatus.EXPIRED},
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
    public Optional<Map<PRStatus, Long>> getPassRequestCountByStatusForUser(String authorId) {
        log.info("Getting passRequests count by status for user");
        List<PassRequest> requests = getUserRequests(authorId);
        Map<PRStatus, Long> statusesCount = new HashMap<>();
        for (PRStatus status : PRStatus.values()) {
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
    public Optional<List<PassRequestUser>> getPassRequestUsers(PRDTO dto) {
        Optional<PassRequest> request = getPR(dto);

        if (request.isPresent()) {
            if (request.get().getType() == PRType.SINGLE) {
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
     * Удаление заявки по id
     * @param id заявки
     * @return в случае, если заявка была найдена и удалена,
     * возвращается она, если нет - Optional.empty()
     */
    @Override
    public Optional<PassRequest> deletePassRequestById(UUID id) {
        Optional<PassRequest> passRequest = getPR(id);
        if (passRequest.isPresent()) {
            passRequestRepository.deleteById(id);
            log.info("pass request with id: {} was deleted", id);
            return passRequest;
        }
        log.info("pass request with id: {} wasn't found", id);
        return Optional.empty();
    }

    /**
     * Создать одиночную заявку
     * @param passRequestDTO заявки
     * @param userId идентификатор пользователя
     * @return созданная заявка
     */
    private Optional<PassRequest> createSinglePassRequest(PRDTO passRequestDTO, String userId) {
        UserDTO author = scosAPIService.getUserDetails(userId).orElseThrow();
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
        Optional<StudentDTO> student =
                vamAPIService.getStudents("email", author.getEmail())
                        .orElseThrow()
                        .getResults()
                        .stream()
                        .filter(s -> s.getStudy_year() != null)
                        .findFirst();

        if (student.isPresent()) {
            Optional<OrganizationProfileDTO> authorOrganization =
                    scosAPIService.getOrganizationByGlobalId(
                            student.get().getOrganization_id()
                    );
            if (authorOrganization.isPresent()) {
                Optional<OrganizationProfileDTO> targetOrganization =
                        scosAPIService.getOrganizationByGlobalId(
                                passRequestDTO.getTargetUniversityId()
                        );

                if (targetOrganization.isPresent()) {
                    return Optional.of(new PassRequest(
                            userId,
                            author.getFirst_name(),
                            author.getLast_name(),
                            author.getPatronymic_name(),
                            student.get().getOrganization_id(),
                            authorOrganization.get().getShort_name(),
                            passRequestDTO.getStartDate(),
                            passRequestDTO.getEndDate(),
                            passRequestDTO.getStatus(),
                            passRequestDTO.getType(),
                            passRequestDTO.getTargetUniversityAddress(),
                            targetOrganization.get().getShort_name(),
                            passRequestDTO.getTargetUniversityId(),
                            PRUtils.getRequestNumber(passRequestRepository),
                            author.getPhoto_url())
                    );
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Получить список заявок по id создателя и статусу
     * @param requests список заявок
     * @param statuses стутус заявки
     * @param page номер страницы
     * @param pageSize размер страницы
     * @return список отобранных заявок по критериям выше
     */
    private GenericResponseDTO<PassRequest> getPRByStatusWithPaginationForUser(
            List<PassRequest> requests,
            PRStatus[] statuses,
            Long page,
            Long pageSize) {
        List<PassRequest> filteredRequests = new LinkedList<>();
        for (PRStatus status : statuses) {
            filteredRequests.addAll(
                    requests.stream()
                            .filter(request -> request.getStatus() == status)
                            .collect(Collectors.toList())
            );
        }
        return new GenericResponseDTO<>(
                page,
                pageSize,
                filteredRequests.size() % pageSize == 0 ?
                        filteredRequests.size() / pageSize : filteredRequests.size() / pageSize + 1,
                (long) filteredRequests.size(),
                PRUtils.paginateRequests(filteredRequests, page, pageSize)
        );
    }

    /**
     * Получить список всех заявок пользователя кроме скрытых
     * Скрытыми являются автоматические заявки
     * @param userId идентификатор пользователя
     * @return список заявок пользователя
     */
    private List<PassRequest> getUserRequests(String userId) {
        List<PassRequest> requests;
        requests = passRequestRepository.findAllByAuthorId(userId);
        requests.addAll(passRequestUserRepository.getByScosId(userId)
                .stream()
                .map(PassRequestUser::getPassRequestId)
                .map(this::getPassRequestById)
                .map(Optional::get)
                .collect(Collectors.toList()));
        requests.removeIf(request ->
                request.getStatus() == PRStatus.ACCEPTED && request.getChangeLog().isEmpty()
        );

        return requests;
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
     * Получить азявку по id
     * @param id id заявки
     * @return заявка
     */
    private Optional<PassRequest> getPR(UUID id) {
        return PRUtils.getPR(id, passRequestRepository);
    }
}
