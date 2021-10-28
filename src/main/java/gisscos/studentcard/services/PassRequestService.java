package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassRequestService {

    PassRequest addPassRequest(PassRequestDTO passRequestDTO);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<List<PassRequest>> getPassRequestsByUniversity(UUID universityId);

    Integer getPassRequestsNumberByUniversity(UUID universityId);

    Optional<List<PassRequest>> getPassRequestsByUserId(UUID userId);

    Optional<PassRequest> getPassRequestById(Long id);

    Optional<List<PassRequest>> getPassRequestByStatus(PassRequestDTO dto);

    Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequest(PassRequestDTO passRequestDTO);

    Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto);

    Optional<PassRequest> deletePassRequestById(Long id);

    Optional<PassRequestUser> deleteUserFromPassRequest(PassRequestUserDTO dto);

    Optional<List<PassRequest>> deleteExpiredPassRequests();
}
