package ru.edu.online.services;

import ru.edu.online.entities.PassRequestComment;
import ru.edu.online.entities.dto.PRCommentDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPRCommentsService {

    Optional<PassRequestComment> addCommentToPassRequest(PRCommentDTO dto);

    Optional<List<PassRequestComment>> getPassRequestComments(UUID passRequestId);

    Optional<PassRequestComment> updateComment(PRCommentDTO dto);

    Optional<PassRequestComment> deletePassRequestComment(PRCommentDTO dto);
}
