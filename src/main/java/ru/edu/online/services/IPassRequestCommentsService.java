package ru.edu.online.services;

import ru.edu.online.entities.PassRequestComment;
import ru.edu.online.entities.dto.PassRequestCommentDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPassRequestCommentsService {

    Optional<PassRequestComment> addCommentToPassRequest(PassRequestCommentDTO dto);

    Optional<List<PassRequestComment>> getPassRequestComments(UUID passRequestId);

    Optional<PassRequestComment> updateComment(PassRequestCommentDTO dto);

    Optional<PassRequestComment> deletePassRequestComment(PassRequestCommentDTO dto);
}
