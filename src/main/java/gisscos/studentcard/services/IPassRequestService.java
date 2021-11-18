package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.entities.enums.RequestsStatusForAdmin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPassRequestService {

    PassRequest addPassRequest(PassRequestDTO passRequestDTO);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<List<PassRequest>> getPassRequestsForAdmin(
            RequestsStatusForAdmin status,
            String targetUniversityId,
            Long page,
            Optional<String> search
    );

    Optional<List<PassRequest>> getPassRequestsByUserId(String userId);

    Optional<PassRequest> getPassRequestById(UUID id);

    Optional<List<PassRequest>> getPassRequestByStatus(PassRequestDTO dto, Long page, Long pageSize);

    Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto);

    Optional<List<PassRequest>> getExpiredPassRequests();

    Optional<PassRequest> updatePassRequest(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequestStatus(PassRequestDTO dto);

    Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto);

    Optional<PassRequest> deletePassRequestById(UUID id);

    Optional<List<PassRequestUser>> deleteUserFromPassRequest(PassRequestUserDTO[] dto);
}
