package ru.edu.online.services;

import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.dto.PRDTO;
import ru.edu.online.entities.enums.PRStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IPRUserService {

    Optional<PassRequest> addSinglePassRequest(PRDTO passRequestDTO, String userId);

    Optional<ResponseDTO<PassRequest>> getPassRequestByStatusForUser(String authorId,
                                                                     String status,
                                                                     Long page,
                                                                     Long pageSize);

    Optional<Map<PRStatus, Long>> getPassRequestCountByStatusForUser(String authorId);

    Optional<ResponseDTO<PassRequest>> getAcceptedPassRequests(String authorId);

    Optional<List<PassRequest>> getPassRequestsByUserId(String userId);

    Optional<List<PassRequestUser>> getPassRequestUsers(PRDTO passRequestDTO);

    Optional<PassRequest> getPassRequestById(UUID id);

    Optional<PassRequest> deletePassRequestById(UUID id);
}
