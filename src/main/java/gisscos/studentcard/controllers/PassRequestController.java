package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.PassRequestComment;
import gisscos.studentcard.entities.PassRequestUser;
import gisscos.studentcard.entities.dto.PassRequestCommentDTO;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.services.PassRequestCommentsService;
import gisscos.studentcard.services.PassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для работы с заявками
 */
@RestController
@RequestMapping("/pass_requests")
public class PassRequestController {

    private final PassRequestService passRequestService;
    private final PassRequestCommentsService passRequestCommentsService;

    @Autowired
    public PassRequestController(PassRequestService passRequestService,
                                 PassRequestCommentsService passRequestCommentsService) {
        this.passRequestService = passRequestService;
        this.passRequestCommentsService = passRequestCommentsService;
    }

    /**
     * Создание новой заявки
     * @param dto DTO заявки
     * @return созданная заявка
     */
    @PostMapping("/add")
    public ResponseEntity<PassRequest> addPassRequest(@RequestBody PassRequestDTO dto) {
        return new ResponseEntity<>(passRequestService.addPassRequest(dto), HttpStatus.CREATED);
    }

    /**
     * Добавление пользователя в заявку
     * @param dto пользователя в заявке
     * @return если заявка найдена, список пользователей заявки с учётом уже добавленного
     */
    @PostMapping("/add_user")
    public ResponseEntity<List<PassRequestUser>> addUserToPassRequest(@RequestBody PassRequestUserDTO dto) {
        return passRequestService.addUserToPassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Добавление комментария к заявке
     * @param dto комментария
     * @return добавленный комментарий
     */
    @PostMapping("/comments/add")
    public ResponseEntity<PassRequestComment> addCommentToPassRequest(@RequestBody PassRequestCommentDTO dto) {
        return passRequestCommentsService.addCommentToPassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявки по id
     * @param id заявки
     * @return заявка
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<PassRequest> getPassRequestById(@PathVariable Long id) {
        return passRequestService.getPassRequestById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявки по id пользователя
     * @param id заявки
     * @return заявка
     */
    @GetMapping("/user/get/{id}")
    public ResponseEntity<List<PassRequest>> getPassRequestByUserId(@PathVariable Long id) {
        return passRequestService.getPassRequestsByUserId(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявок по статусу
     * @param dto заявки
     * @return заявки
     */
    @GetMapping("/get/status")
    public ResponseEntity<List<PassRequest>> getPassRequestByStatus(@RequestBody PassRequestDTO dto) {
        return passRequestService.getPassRequestByStatus(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявок для обработки администратором ООВО
     * @param universityId идентификатор ООВО
     * @return список заявок для обработки
     */
    @GetMapping("/get/requests/{universityId}")
    public ResponseEntity<List<PassRequest>> getPassRequestsForProcessing(@PathVariable Long universityId) {
        return passRequestService.getPassRequestsByUniversity(universityId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение количества заявок для обработки администратором ООВО
     * @param universityId идентификатор ООВО
     * @return количество заявок для обработки
     */
    @GetMapping("/get/requests/count/{universityId}")
    public ResponseEntity<Integer> getPassRequestsNumberForProcessing(@PathVariable Long universityId) {
        return ResponseEntity.of(
                Optional.of(passRequestService.getPassRequestsNumberByUniversity(universityId))
        );
    }

    /**
     * Получить список пользователей групповой заявки
     * @param dto заявки. Необходимо передать только id заявки
     * @return список пользователей заявки
     */
    @GetMapping("/get/users")
    public ResponseEntity<List<PassRequestUser>> getPassRequestUsers(@RequestBody PassRequestDTO dto) {
        return passRequestService.getPassRequestUsers(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение комментариев заявки
     * @param passRequestId id заявки
     * @return список комментариев
     */
    @GetMapping("/comments/{passRequestId}")
    public ResponseEntity<List<PassRequestComment>> getCommentsByPassRequest(@PathVariable Long passRequestId) {
        return passRequestCommentsService.getPassRequestComments(passRequestId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Редактирование заявки
     * @param dto DTO заявки
     * @return отредактированная заявка
     */
    @PutMapping("/edit")
    public ResponseEntity<PassRequest> editPassRequest(@RequestBody PassRequestDTO dto) {
        return passRequestService.updatePassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Редактирование комментария
     * @param dto комментария с id
     * @return отредактированный комментарий
     */
    @PutMapping("/comments/edit")
    public ResponseEntity<PassRequestComment> editPassRequestComment(@RequestBody PassRequestCommentDTO dto) {
        return passRequestCommentsService.updateComment(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Отмена заявки создателем
     * @param dto пользователя заявки
     * @return отменённая заявка
     */
    @PutMapping("/cancel")
    public ResponseEntity<PassRequest> cancelPassRequest(@RequestBody PassRequestUserDTO dto) {
        return passRequestService.cancelPassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление заявки по id
     * @param id заявки
     * @return статус OK (временное решение до spring security)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PassRequest> deletePassRequestById(@PathVariable Long id) {
        return passRequestService.deletePassRequestById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление пользователя из заявки
     * @param dto пользователя в заявке
     * @return удаленный пользователь, если таковой найден
     */
    @DeleteMapping("/delete_user")
    public ResponseEntity<PassRequestUser> deleteUserFromPassRequest(@RequestBody PassRequestUserDTO dto) {
        return passRequestService.deleteUserFromPassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление просроченных заявок
     * @return список просроченных заявок, которые были удалены
     */
    @DeleteMapping("/delete/expired_requests")
    public ResponseEntity<List<PassRequest>> deleteExpiredPassRequests() {
        return ResponseEntity.of(passRequestService.deleteExpiredPassRequests());
    }

    /**
     * Удаление комментария
     * @param dto комментария с id
     * @return удалённый комментарий
     */
    @DeleteMapping("/comments/delete")
    public ResponseEntity<PassRequestComment> deletePassRequestComment(@RequestBody PassRequestCommentDTO dto) {
        return passRequestCommentsService.deletePassRequestComment(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
