package ru.edu.online.services;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.PRDTO;
import ru.edu.online.entities.dto.PRUserDTO;
import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.dto.UserDTO;
import ru.edu.online.entities.enums.PRStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IPRAdminService {

    Optional<PassRequest> addGroupPassRequest(PRDTO passRequestDTO, String userId);

    Optional<List<PassRequestUser>> addUserToPassRequest(PRUserDTO passRequestUserDTO);

    Optional<Map<PRStatus, Integer>> getPassRequestsCountByStatusForAdmin(String userId);

    Optional<ResponseDTO<PassRequest>> getPassRequestsForAdmin(String status,
                                                               Long page,
                                                               Long pageSize,
                                                               String search,
                                                               String userId);

    Optional<ResponseDTO<UserDTO>> getAdminUniversityUsers(String userId,
                                                           long page,
                                                           long usersPerPage,
                                                           String search);

    Optional<PassRequest> updatePassRequestStatus(PRDTO passRequestDTO);

    Optional<PassRequest> updatePassRequestDates(PRDTO passRequestDTO);

    Optional<List<PassRequestUser>> deleteUserFromPassRequest(PRUserDTO[] passRequestUsersArray);
}
