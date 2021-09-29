package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;

import java.util.List;
import java.util.Optional;

public interface PassRequestService {

    PassRequest createPassRequest(PassRequestDTO passRequestDTO);

    Optional<PassRequest> getPassRequestById(Long id);

    Optional<PassRequest> updatePassRequest(PassRequestDTO passRequestDTO);

    Optional<PassRequest> deletePassRequestById(Long id);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<PassRequestUser> deleteUserFromPassRequest(PassRequestUserDTO dto);

    Optional<List<PassRequest>> getPassRequestsByUniversity(Long universityId);
}
