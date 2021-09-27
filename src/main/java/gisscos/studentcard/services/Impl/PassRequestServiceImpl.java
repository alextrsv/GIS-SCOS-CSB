package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.entities.enums.PassRequestType;
import gisscos.studentcard.repositories.PassRequestRepository;
import gisscos.studentcard.services.PassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Сервис для работы с заявками.
 */
@Service
public class PassRequestServiceImpl implements PassRequestService {

    private final PassRequestRepository passRequestRepository;

    @Autowired
    public PassRequestServiceImpl(PassRequestRepository passRequestRepository) {
        this.passRequestRepository = passRequestRepository;
    }

    /**
     * Добавление заявки в БД. Если заявка групповая, со
     * @param passRequestDTO DTO заявки
     * @return добавленная заявка
     */
    @Override
    public PassRequest createPassRequest(PassRequestDTO passRequestDTO) {

        PassRequest passRequest = new PassRequest(
                passRequestDTO.getUserId(), passRequestDTO.getUniversityId(),
                passRequestDTO.getStartDate(), passRequestDTO.getEndDate(),
                passRequestDTO.getStatus(), passRequestDTO.getType(),
                passRequestDTO.getComment()
        );

        if (passRequestDTO.getType() == PassRequestType.GROUP) {
            passRequest.setUsers(new ArrayList<>());
            PassRequestUser passRequestUser;

            for ( PassRequestUserDTO user : passRequestDTO.getUsers() ) {
                passRequestUser = new PassRequestUser(user.getPassRequestId(), user.getUserId());
                passRequest.getUsers().add(passRequestUser);
            }
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
     * @param passRequestDTO DTO обновленной заявки
     * @return обновленная заявка
     */
    @Override
    public Optional<PassRequest> updatePassRequest(PassRequestDTO passRequestDTO) {
        Optional<PassRequest> passRequest = passRequestRepository.findById(passRequestDTO.getId());

        if (passRequest.isPresent()) {
            passRequest.get().setUserId(passRequestDTO.getUserId());
            passRequest.get().setStatus(passRequestDTO.getStatus());
            passRequest.get().setType(passRequestDTO.getType());
            passRequest.get().setComment(passRequestDTO.getComment());
            passRequest.get().setStartDate(passRequestDTO.getStartDate());
            passRequest.get().setEndDate(passRequestDTO.getEndDate());
            passRequest.get().setUniversityId(passRequestDTO.getUniversityId());
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

    @Override
    public Optional<PassRequest> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO) {
        Optional<PassRequest> passRequest = passRequestRepository.findById(passRequestUserDTO.getPassRequestId());

        if (passRequest.isPresent()) {
            PassRequestUser passRequestUser = new PassRequestUser(
                    passRequestUserDTO.getPassRequestId(),
                    passRequestUserDTO.getUserId()
            );
            passRequest.get().getUsers().add(passRequestUser);
            passRequestRepository.save(passRequest.get());
            return passRequest;
        } else
            return Optional.empty();
    }
}
