package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.PassRequestDTO;

import java.util.Optional;

public interface PassRequestService {

    PassRequest createPassRequest(PassRequestDTO passRequestDTO);
    Optional<PassRequest> getPassRequestById(Long id);
    Optional<PassRequest> updatePassRequest(PassRequestDTO passRequestDTO);
    Optional<PassRequest> deletePassRequestById(Long id);
}
