package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestComment;
import ru.edu.online.entities.dto.PassRequestCommentDTO;
import ru.edu.online.repositories.IPassRequestCommentRepository;
import ru.edu.online.repositories.IPassRequestRepository;
import ru.edu.online.services.IPassRequestCommentsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с комментариями заявок
 */
@Slf4j
@Service
public class PassRequestCommentsServiceImpl implements IPassRequestCommentsService {

    private final IPassRequestRepository passRequestRepository;
    private final IPassRequestCommentRepository passRequestCommentRepository;

    @Autowired
    public PassRequestCommentsServiceImpl(IPassRequestRepository passRequestRepository,
                                          IPassRequestCommentRepository passRequestCommentRepository) {
        this.passRequestRepository = passRequestRepository;
        this.passRequestCommentRepository = passRequestCommentRepository;
    }

    /**
     * Добавление комментария
     * @param dto комментария
     * @return добавленный комментарий
     */
    @Override
    public Optional<PassRequestComment> addCommentToPassRequest(PassRequestCommentDTO dto) {
        Optional<PassRequest> request = getRequest(dto);

        if (request.isPresent()) {
            PassRequestComment comment = new PassRequestComment();
            comment.setComment(dto.getComment());
            comment.setPassRequestId(dto.getPassRequestId());
            comment.setAuthorId(dto.getAuthorId());
            comment.setCreationDate(LocalDateTime.now());
            comment.setEditDate(LocalDateTime.now());

            request.get().getComments().add(comment);
            passRequestCommentRepository.save(comment);
            log.info("Comment added to passRequest");
            return Optional.of(comment);
        }

        log.warn("Pass request not found");
        return Optional.empty();
    }

    /**
     * Получение комментириев по id заявки
     * @param passRequestId id заявки
     * @return список комментариев
     */
    @Override
    public Optional<List<PassRequestComment>> getPassRequestComments(UUID passRequestId) {
        Optional<PassRequest> request = getRequest(passRequestId);

        log.info("Getting comments from pass request");
        return request.map(PassRequest::getComments);
    }

    /**
     * Редактирование комментария
     * @param dto комментария
     * @return отредактированный комментарий
     */
    @Override
    public Optional<PassRequestComment> updateComment(PassRequestCommentDTO dto) {
        Optional<PassRequestComment> comment = getComment(dto);
        if (comment.isPresent()) {
            comment.get().setComment(dto.getComment());
            comment.get().setPassRequestId(dto.getPassRequestId());
            comment.get().setAuthorId(dto.getAuthorId());
            comment.get().setEditDate(LocalDateTime.now());

            passRequestCommentRepository.save(comment.get());
            log.info("Comment has successfully updated");
            return comment;
        }
        log.warn("Pass request not found");
        return Optional.empty();
    }

    /**
     * Удаление комментария по его id
     * @param dto комментария с id
     * @return удалённый комментарий
     */
    @Override
    public Optional<PassRequestComment> deletePassRequestComment(PassRequestCommentDTO dto) {
        Optional<PassRequestComment> comment = getComment(dto);

        if (comment.isPresent()) {
            passRequestCommentRepository.deleteById(dto.getId());
            log.info("Comment has successfully deleted");
            return comment;
        }
        log.warn("Pass request not found");
        return Optional.empty();
    }

    /**
     * Получить комментарий по id
     * @param dto комментария
     * @return комментарий
     */
    private Optional<PassRequestComment> getComment(PassRequestCommentDTO dto) {
        return passRequestCommentRepository.findById(dto.getId());
    }

    /**
     * Получить заявку по id
     * @param dto комментария
     * @return заявка
     */
    private Optional<PassRequest> getRequest(PassRequestCommentDTO dto) {
        return passRequestRepository.findById(dto.getPassRequestId());
    }

    /**
     * Получить заявку по id
     * @param passRequestId id заявки
     * @return заявка
     */
    private Optional<PassRequest> getRequest(UUID passRequestId) {
        return passRequestRepository.findById(passRequestId);
    }
}
