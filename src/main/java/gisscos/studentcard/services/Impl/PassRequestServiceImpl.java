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
import gisscos.studentcard.repositories.IDynamicQRUserRepository;
import gisscos.studentcard.repositories.IPassRequestChangeLogRepository;
import gisscos.studentcard.repositories.IPassRequestRepository;
import gisscos.studentcard.repositories.IPassRequestUserRepository;
import gisscos.studentcard.services.IPassRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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

    @Autowired
    public PassRequestServiceImpl(IPassRequestRepository passRequestRepository,
                                  IPassRequestUserRepository passRequestUserRepository,
                                  IPassRequestChangeLogRepository passRequestChangeLogRepository, IDynamicQRUserRepository dynamicQRUserRepository) {
        this.passRequestRepository = passRequestRepository;
        this.passRequestUserRepository = passRequestUserRepository;
        this.passRequestChangeLogRepository = passRequestChangeLogRepository;
        this.dynamicQRUserRepository = dynamicQRUserRepository;
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
                dto.getTargetUniversityName(), dto.getUniversityName()
        );

//        ADD NEW USER FROM SINGLE REQUEST
        if (dto.getType() == PassRequestType.SINGLE && !dynamicQRUserRepository.existsByUserId(dto.getUserId())){
            dynamicQRUserRepository.save(new DynamicQRUser(dto.getUserId(), dto.getUniversityId()));
        }

        if (dto.getType() == PassRequestType.GROUP) {
            long id = passRequestRepository.save(passRequest).getId();

            for (PassRequestUserDTO user : dto.getUsers()) {
                user.setPassRequestId(id);
                addUserToPassRequest(user);
                if (!dynamicQRUserRepository.existsByUserId(user.getUserId()))
                    dynamicQRUserRepository.save(new DynamicQRUser(user.getUserId(), dto.getUniversityId()));
            }
            if (getPassRequestById(id).isPresent())
                return getPassRequestById(id).get();
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
            if (passRequestUserRepository
                    .existsByPassRequestIdAndUserId( dto.getPassRequestId(), dto.getUserId())) {
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
     * @param id заявки
     * @return заявка
     */
    @Override
    public Optional<PassRequest> getPassRequestById(Long id) {
        log.info("getting pass request by id: {}", id);
        return getPassRequest(id);
    }

    /**
     * Получить список заявок по id пользователя
     * @param id пользователя
     * @return список заявок
     */
    @Override
    public Optional<List<PassRequest>> getPassRequestsByUserId(Long id) {
        log.info("getting pass request by user id: {}", id);
        List<PassRequest> requestList = passRequestRepository.findAllByUserId(id);

        if (requestList.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(requestList);
    }

    /**
     * Получить список заявок по статусу
     * @param dto заявки
     * @param page номер страницы
     * @param pageSize размер страницы
     * @return список заявок с определенным статусом
     */
    @Override
    public Optional<List<PassRequest>> getPassRequestByStatus(PassRequestDTO dto,
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

    /**
     * Получение заявок для обработки.
     * @param universityId идентификатор ООВО
     * @return список заявок для обработки
     */
    @Override
    public Optional<List<PassRequest>> getPassRequestsByUniversity(String universityId) {
        List<PassRequest> targetRequestList =
                passRequestRepository.findAllByTargetUniversityId(universityId);

        targetRequestList = targetRequestList.stream()
                .filter(
                        request -> request.getStatus() == PassRequestStatus.TARGET_ORGANISATION_REVIEW
                )
                .collect(Collectors.toList());
        log.info("collect requests sent for consideration to the target OOVO");
        List<PassRequest> userRequestList = passRequestRepository.findAllByUniversityId(universityId);

        userRequestList = userRequestList.stream()
                .filter(
                        request -> request.getStatus() == PassRequestStatus.USER_ORGANISATION_REVIEW
                )
                .collect(Collectors.toList());
        log.info("collect requests sent for consideration to their OOVO.");
        targetRequestList.addAll(userRequestList);
        log.info("collect all requests together");
        return Optional.of(targetRequestList);
    }

    @Override
    public Optional<List<PassRequest>> getPassRequestsByUserId(UUID userId){
        return Optional.ofNullable(passRequestRepository.findAllByUserId(userId));
    }
    /**
     * Получение количества заявок для обработки.
     * @param universityId идентификатор ООВО
     * @return количество заявок для обработки
     */
    @Override
    public Integer getPassRequestsNumberByUniversity(String universityId) {
        Optional<List<PassRequest>> list = getPassRequestsByUniversity(universityId);
        log.info("Calculating number of passRequests by universityId");
        return list.map(List::size).orElse(0);
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
            if (status == PassRequestStatus.USER_ORGANISATION_REVIEW
                 || status == PassRequestStatus.TARGET_ORGANISATION_REVIEW) {

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
    public Optional<PassRequest> deletePassRequestById(Long id) {
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
    public Optional<PassRequestUser> deleteUserFromPassRequest(PassRequestUserDTO dto) {
        Optional<PassRequest> passRequest = getPassRequest(dto);

        // Если заявка существует и является групповой
        if (passRequest.isPresent() && passRequest.get().getType() == PassRequestType.GROUP) {

            // Из списка пользоватлелей заявки выбираются тот,
            // id которого совпадает с искомым id из списка и
            // удаляется если существует.
            passRequest.get()
                    .getUsers()
                    .stream()
                    .filter(
                            user -> (Objects.equals(
                                    user.getUserId(), dto.getUserId())
                            )
                    )
                    .findAny()
                    .ifPresent(
                            user -> passRequestUserRepository.deleteById(user.getId())
                    );
            log.info("user with id: {} has been deleted from pass request", dto.getUserId());
            // Удалённый пользователь (по сути PassRequestUserDTO)
            return passRequest.get()
                    .getUsers()
                    .stream()
                    .filter(
                            user -> (Objects.equals(
                                    user.getUserId(), dto.getUserId())
                            )
                    )
                    .findAny();
        } else {
            log.warn("user with id: {} hasn't been deleted from pass request " +
                    "due pass request type isn't \"GROUP\" or pass request isn't present", dto.getUserId());
            return Optional.empty();
        }
    }

    /**
     * Удаление просроченных заявок
     * @return список удаленных заявок
     */
    @Override
    public Optional<List<PassRequest>> deleteExpiredPassRequests() {
        // Поиск просроченных заявок, изменение их статуса на EXPIRED
        checkExpiredPassRequests();

        // Поиск заявок со статусом EXPIRED и CANCELED_BY_CREATOR
        List<PassRequest> expiredList =
                passRequestRepository.findAllByStatus(PassRequestStatus.EXPIRED);
        expiredList
                .addAll(
                        passRequestRepository.findAllByStatus(PassRequestStatus.CANCELED_BY_CREATOR)
                );

        for (PassRequest request : expiredList) {
            passRequestRepository.deleteById(request.getId());
        }
        log.info("expired pass requests has been deleted");

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
    private Optional<PassRequest> getPassRequest(Long id) {
        return passRequestRepository.findById(id);
    }
}
