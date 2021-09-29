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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для работы с заявками.
 */
@Service
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
                return Optional.empty();
            }

            PassRequestUser passRequestUser = new PassRequestUser(
                    dto.getPassRequestId(),
                    dto.getUserId()
            );
            passRequestUserRepository.save(passRequestUser);
            return Optional.of(passRequestUserRepository.findAllByPassRequestId(passRequest.get().getId()));
        } else
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

        List<PassRequest> userRequestList = passRequestRepository.findAllByUniversityId(universityId);

        userRequestList = userRequestList.stream()
                .filter(
                        request -> request.getStatus() == PassRequestStatus.USER_ORGANISATION_REVIEW
                )
                .collect(Collectors.toList());
        targetRequestList.addAll(userRequestList);
        return Optional.of(targetRequestList);
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
            return passRequest;
        }

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
        } else
           return Optional.empty();
    }

    @Override
    public Optional<List<PassRequest>> deleteExpiredPassRequests() {
        List<PassRequest> expiredList =
                passRequestRepository.findAllByStatus(PassRequestStatus.EXPIRED);

        for (PassRequest request : expiredList) {
            passRequestRepository.deleteById(request.getId());
        }

        return Optional.of(expiredList);
    }
}
