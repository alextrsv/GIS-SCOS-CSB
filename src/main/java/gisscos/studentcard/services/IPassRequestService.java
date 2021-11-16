package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;

import java.util.List;
import java.util.Optional;

public interface IPassRequestService {

    PassRequest addPassRequest(PassRequestDTO passRequestDTO);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<List<PassRequest>> getPassRequestsForProcessing(Long universityId, Long page);

    Optional<List<PassRequest>> getPassRequestsInProcessing(Long universityId, Long page);

    Optional<List<PassRequest>> getProcessedPassRequests(Long universityId, Long page);

    Optional<PassRequest> getPassRequestById(Long id);

    Optional<List<PassRequest>> getPassRequestsByUserId(Long id);

    Optional<List<PassRequest>> getPassRequestByStatus(PassRequestDTO dto, Long page, Long pageSize);

    Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequest(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequestStatus(PassRequestDTO dto);

    Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto);

    Optional<PassRequest> deletePassRequestById(Long id);

    Optional<PassRequestUser> deleteUserFromPassRequest(PassRequestUserDTO dto);

    Optional<List<PassRequest>> deleteExpiredPassRequests();
}
