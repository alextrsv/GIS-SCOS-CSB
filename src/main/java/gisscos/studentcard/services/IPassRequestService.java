package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.RequestsStatusForAdmin;

import java.util.List;
import java.util.Map;
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

    Optional<List<PassRequest>> getPassRequestCountByStatusForAdmin(String authorId, PassRequestStatus status);

    Optional<List<PassRequest>> getPassRequestsByUserId(String userId);

    Optional<PassRequest> getPassRequestById(UUID id, String authorId);

    Optional<List<PassRequest>> getPassRequestByStatusForUniversity(PassRequestDTO dto, Long page, Long pageSize);

    Optional<List<PassRequest>> getPassRequestByStatusForUser(String authorId, PassRequestStatus status, Long page, Long pageSize);

    Optional<Map<PassRequestStatus, Long>> getPassRequestCountByStatusForUser(String authorId);

    Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto);

    Optional<List<PassRequest>> getExpiredPassRequests();

    Optional<PassRequest> updatePassRequest(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequestStatus(PassRequestDTO dto);

    Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto);

    Optional<PassRequest> deletePassRequestById(UUID id);

    Optional<List<PassRequestUser>> deleteUserFromPassRequest(PassRequestUserDTO[] dto);
}
