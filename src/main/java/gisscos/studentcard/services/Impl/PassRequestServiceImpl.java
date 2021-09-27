package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.entities.enums.PassRequestType;
import gisscos.studentcard.repositories.PassRequestRepository;
import gisscos.studentcard.repositories.PassRequestUserRepository;
import gisscos.studentcard.services.PassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public PassRequest createPassRequest(PassRequestDTO dto) {

        PassRequest passRequest = new PassRequest(
                dto.getUserId(), dto.getUniversityId(),
                dto.getStartDate(), dto.getEndDate(),
                dto.getStatus(), dto.getType(),
                dto.getComment()
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
     * Получение заявки по id
     * @param id заявки
     * @return заявка
     */
    @Override
    public Optional<PassRequest> getPassRequestById(Long id) {
        return passRequestRepository.findById(id);
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
            passRequest.get().setUserId(dto.getUserId());
            passRequest.get().setStatus(dto.getStatus());
            passRequest.get().setType(dto.getType());
            passRequest.get().setComment(dto.getComment());
            passRequest.get().setStartDate(dto.getStartDate());
            passRequest.get().setEndDate(dto.getEndDate());
            passRequest.get().setUniversityId(dto.getUniversityId());
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
}
