package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassRequestComment;
import gisscos.studentcard.entities.dto.PassRequestCommentDTO;

import java.util.List;
import java.util.Optional;

public interface PassRequestCommentsService {

    Optional<PassRequestComment> addCommentToPassRequest(PassRequestCommentDTO dto);

    Optional<List<PassRequestComment>> getPassRequestComments(Long passRequestId);

    Optional<PassRequestComment> updateComment(PassRequestCommentDTO dto);

    Optional<PassRequestComment> deletePassRequestComment(PassRequestCommentDTO dto);
}
