package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.repositories.PassRequestRepository;
import gisscos.studentcard.services.PassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * Добавление заявки в БД
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
    public Optional<PassRequest> updateRequest(PassRequestDTO passRequestDTO) {
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
     */
    @Override
    public void deletePassRequestById(Long id) {
        passRequestRepository.deleteById(id);
    }
}
