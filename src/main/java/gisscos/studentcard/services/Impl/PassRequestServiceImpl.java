package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import gisscos.studentcard.repositories.PassRequestRepository;
import gisscos.studentcard.repositories.PassRequestUserRepository;
import gisscos.studentcard.services.PassRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для работы с заявками.
 */
@Service
@Slf4j
public class PassRequestServiceImpl implements PassRequestService {

    private final PassRequestRepository passRequestRepository;
    private final PassRequestUserRepository passRequestUserRepository;

    @Autowired
    public PassRequestServiceImpl(PassRequestRepository passRequestRepository,
                                  PassRequestUserRepository passRequestUserRepository) {
        this.passRequestRepository = passRequestRepository;
        this.passRequestUserRepository = passRequestUserRepository;
    }

    /**
     * Добавление заявки в БД. Если заявка групповая, со
     * @param dto DTO заявки
     * @return добавленная заявка
     */
    @Override
    public PassRequest addPassRequest(PassRequestDTO dto) {

        PassRequest passRequest = new PassRequest(
                dto.getUserId(), dto.getTargetUniversityId(),
                dto.getUniversityId(), dto.getStartDate(),
                dto.getEndDate(), dto.getStatus(),
                dto.getType(), dto.getComment()
        );

        if (dto.getType() == PassRequestType.GROUP) {
            long id = passRequestRepository.save(passRequest).getId();

            for ( PassRequestUserDTO user : dto.getUsers() ) {
                user.setPassRequestId(id);
                addUserToPassRequest(user);
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
        Optional<PassRequest> passRequest = passRequestRepository.findById(dto.getPassRequestId());

        // Если есть такая заявка и она является групповой
        if (passRequest.isPresent() && passRequest.get().getType() == PassRequestType.GROUP) {
            // Если такой пользователь в заявке уже есть
            if (passRequestUserRepository
                    .existsByPassRequestIdAndUserId(dto.getUserId(), dto.getPassRequestId())) {
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
        return passRequestRepository.findById(id);
    }

    /**
     * Получить список заявок по статусу
     * @param status статус заявки
     * @return список заявок с определенным статусом
     */
    @Override
    public Optional<List<PassRequest>> getPassRequestByStatus(String status) {
        try {
            PassRequestStatus requestStatus = PassRequestStatus.of(status);
            log.info("Getting passRequests by status");
            return Optional.of(passRequestRepository.findAllByStatus(requestStatus));
        } catch (IllegalArgumentException ex) {
            log.warn("Cannot resolve {} as PassRequestStatus.", status);
            return Optional.empty();
        }
    }

    /**
     * Получить список пользователей групповой заявки.
     * @param dto заявки
     * @return список пользователей (включая автора) заявки или Optional.empty
     * если заявка одиночная или вообще не найдена.
     */
    @Override
    public Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto) {
        Optional<PassRequest> request =
                passRequestRepository.findById(dto.getId());

        if (request.isPresent()) {
            if (request.get().getType() == PassRequestType.SINGLE) {
                log.warn("Pass request with {} id has single type.",
                        request.get().getId());

                return Optional.empty();
            }
            log.info("Getting users from pass request with id {}",
                    request.get().getId());

            // Добавление автора в список пользователей заявки
            request.get().getUsers().add(
                    new PassRequestUser(
                            request.get().getId(),
                            request.get().getUserId()
                    )
            );

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
    public Optional<List<PassRequest>> getPassRequestsByUniversity(Long universityId) {
        List<PassRequest> targetRequestList = passRequestRepository.findAllByTargetUniversityId(universityId);

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

    /**
     * Получение количества заявок для обработки.
     * @param universityId идентификатор ООВО
     * @return количество заявок для обработки
     */
    @Override
    public Integer getPassRequestsNumberByUniversity(Long universityId) {
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
        Optional<PassRequest> passRequest = passRequestRepository.findById(dto.getId());

        if (passRequest.isPresent()) {
            passRequest.get().setType(dto.getType());
            passRequest.get().setStatus(dto.getStatus());
            passRequest.get().setUserId(dto.getUserId());
            passRequest.get().setComment(dto.getComment());
            passRequest.get().setEndDate(dto.getEndDate());
            passRequest.get().setStartDate(dto.getStartDate());
            passRequest.get().setUniversityId(dto.getUniversityId());
            passRequest.get().setTargetUniversityId(dto.getTargetUniversityId());
            passRequestRepository.save(passRequest.get());

            log.info("pass request with id: {} was updated", dto.getId());
            return passRequest;
        } else
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
        Optional<PassRequest> passRequest = passRequestRepository.findById(id);
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
        Optional<PassRequest> passRequest = passRequestRepository.findById(dto.getPassRequestId());

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
}
