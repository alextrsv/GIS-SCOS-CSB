package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestComment;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestCommentDTO;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;

import java.util.List;
import java.util.Optional;

public interface PassRequestService {

    PassRequest addPassRequest(PassRequestDTO passRequestDTO);

    Optional<List<PassRequestUser>> addUserToPassRequest(PassRequestUserDTO passRequestUserDTO);

    Optional<PassRequestComment> addCommentToPassRequest(PassRequestCommentDTO dto);

    Optional<List<PassRequest>> getPassRequestsByUniversity(Long universityId);

    Integer getPassRequestsNumberByUniversity(Long universityId);

    Optional<PassRequest> getPassRequestById(Long id);

    Optional<List<PassRequest>> getPassRequestByStatus(PassRequestDTO dto);

    Optional<List<PassRequestUser>> getPassRequestUsers(PassRequestDTO dto);

    Optional<List<PassRequestComment>> getPassRequestComments(Long passRequestId);

    Optional<PassRequest> updatePassRequest(PassRequestDTO passRequestDTO);

    Optional<PassRequestComment> updateComment(PassRequestCommentDTO dto);

    Optional<PassRequest> cancelPassRequest(PassRequestUserDTO dto);

    Optional<PassRequest> deletePassRequestById(Long id);

    Optional<PassRequestUser> deleteUserFromPassRequest(PassRequestUserDTO dto);

    Optional<List<PassRequest>> deleteExpiredPassRequests();

    Optional<PassRequestComment> deletePassRequestComment(PassRequestCommentDTO dto);
}
