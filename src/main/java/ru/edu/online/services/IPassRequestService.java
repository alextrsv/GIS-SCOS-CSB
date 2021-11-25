package ru.edu.online.services;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.User;
import ru.edu.online.entities.dto.PassRequestDTO;
import ru.edu.online.entities.dto.PassRequestUserDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.RequestsStatusForAdmin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IPassRequestService {

    PassRequest addPassRequest(PassRequestDTO passRequestDTO);

    Optional<List<User>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<List<PassRequest>> getPassRequestsForAdmin(
            RequestsStatusForAdmin status,
            String targetUniversityId,
            Long page,
            Optional<String> search
    );

    Optional<Map<String, Long>> getPassRequestCountByStatusForAdmin(String authorId);

    Optional<List<PassRequest>> getPassRequestsByUserId(String userId);

    Optional<PassRequest> getPassRequestById(UUID id, String authorId);

    Optional<List<PassRequest>> getPassRequestByStatusForUniversity(PassRequestDTO dto, Long page, Long pageSize);

    Optional<List<PassRequest>> getPassRequestByStatusForUser(String authorId, String status, Long page, Long pageSize);

    Optional<Map<PassRequestStatus, Long>> getPassRequestCountByStatusForUser(String authorId);

    Optional<List<User>> getPassRequestUsers(PassRequestDTO dto);

    Optional<List<PassRequest>> getExpiredPassRequests();

    Optional<PassRequest> updatePassRequest(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequestStatus(PassRequestDTO dto);

    Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto);

    Optional<PassRequest> deletePassRequestById(UUID id);

    Optional<List<User>> deleteUserFromPassRequest(PassRequestUserDTO[] dto);
}
