package ru.edu.online.services;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.PassRequestDTO;
import ru.edu.online.entities.dto.PassRequestUserDTO;
import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.dto.UserDetailsDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.RequestsStatusForAdmin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IPassRequestService {

    Optional<PassRequest> addSinglePassRequest(PassRequestDTO passRequestDTO, String userId);

    Optional<PassRequest> addGroupPassRequest(PassRequestDTO passRequestDTO, String userId);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<Map<PassRequestStatus, Integer>> getPassRequestsCountByStatusForAdmin(String userId);

    Optional<ResponseDTO<PassRequest>> getPassRequestsForAdmin(RequestsStatusForAdmin status,
                                                               Long page,
                                                               Long pageSize,
                                                               String search,
                                                               String userId);

    Optional<List<PassRequest>> getPassRequestsByUserId(String userId);

    Optional<PassRequest> getPassRequestById(UUID id);

    Optional<ResponseDTO<UserDetailsDTO>> getUsersFromAcceptedPassRequestsAdminUniversity(String userId,
                                                                                          long page,
                                                                                          long usersPerPage,
                                                                                          String search);

    Optional<List<PassRequest>> getPassRequestByStatusForUniversity(PassRequestDTO dto, Long page, Long pageSize);

    Optional<ResponseDTO<PassRequest>> getPassRequestByStatusForUser(String authorId, String status, Long page, Long pageSize);

    Optional<Map<PassRequestStatus, Long>> getPassRequestCountByStatusForUser(String authorId);

    Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto);

    void getExpiredPassRequests();

    Optional<PassRequest> updatePassRequest(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequestStatus(PassRequestDTO dto);

    Optional<PassRequest> updatePassRequestDates(PassRequestDTO dto);

    Optional<PassRequest> deletePassRequestById(UUID id);

    Optional<List<PassRequestUser>> deleteUserFromPassRequest(PassRequestUserDTO[] dto);
}
