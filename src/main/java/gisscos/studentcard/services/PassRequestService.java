package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;

import java.util.List;
import java.util.Optional;

public interface PassRequestService {

    PassRequest addPassRequest(PassRequestDTO passRequestDTO);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<List<PassRequest>> getPassRequestsByUniversity(Long universityId);

    Integer getPassRequestsNumberByUniversity(Long universityId);

    Optional<PassRequest> getPassRequestById(Long id);

    Optional<PassRequest> updatePassRequest(PassRequestDTO passRequestDTO);

    Optional<PassRequest> deletePassRequestById(Long id);

    Optional<PassRequestUser> deleteUserFromPassRequest(PassRequestUserDTO dto);

    Optional<List<PassRequest>> deleteExpiredPassRequests();
}
