package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestComment;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.PRCommentDTO;
import ru.edu.online.entities.dto.PRDTO;
import ru.edu.online.entities.dto.PRUserDTO;
import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.enums.PRStatus;
import ru.edu.online.entities.enums.PRType;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IPRAdminService;
import ru.edu.online.services.IPRCommentsService;
import ru.edu.online.services.IPRUserService;
import ru.edu.online.services.IUserDetailsService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Контроллер для работы с заявками
 */
@RestController
@RequestMapping("/pass_requests")
public class PRController {

    /** Сервис комментариев заявок */
    private final IPRCommentsService passRequestCommentsService;
    /** Сервис заявок администратора */
    private final IPRAdminService passRequestAdminService;
    /** Сервис заявок пользователя */
    private final IPRUserService passRequestUserService;
    /** Сервис данных о пользователях */
    private final IUserDetailsService userDetailsService;

    @Autowired
    public PRController(IPRCommentsService passRequestCommentsService,
                        IPRAdminService passRequestAdminService,
                        IPRUserService passRequestUserService,
                        IUserDetailsService userDetailsService) {

        this.passRequestCommentsService = passRequestCommentsService;
        this.passRequestAdminService = passRequestAdminService;
        this.passRequestUserService = passRequestUserService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Создание новой заявки
     * @param dto DTO заявки
     * @return созданная заявка
     */
    @PostMapping("/add")
    public ResponseEntity<PassRequest> addPassRequest(@RequestBody PRDTO dto,
                                                      Principal principal) {
        switch (userDetailsService.getUserRole(principal.getName())) {
            case SUPER_USER:
                switch (dto.getType()) {
                    case GROUP:
                        return passRequestAdminService.addGroupPassRequest(dto, principal.getName()).map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
                    case SINGLE:
                        return passRequestUserService.addSinglePassRequest(dto, principal.getName()).map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
                }
            case ADMIN:
                if (dto.getType() == PRType.GROUP) {
                    return passRequestAdminService.addGroupPassRequest(dto, principal.getName()).map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
                }
            case STUDENT:
                if (dto.getType() == PRType.SINGLE) {
                    return passRequestUserService.addSinglePassRequest(dto, principal.getName()).map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
                }
            default:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Добавление пользователя в заявку
     * @param dto пользователя в заявке
     * @return если заявка найдена, список пользователей заявки с учётом уже добавленного
     */
    @PostMapping("/add_user")
    public ResponseEntity<List<PassRequestUser>> addUserToPassRequest(@RequestBody PRUserDTO dto,
                                                                      Principal principal) {
        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return passRequestAdminService.addUserToPassRequest(dto).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Добавление комментария к заявке
     * @param dto комментария
     * @return добавленный комментарий
     */
    @PostMapping("/comments/add")
    public ResponseEntity<PassRequestComment> addCommentToPassRequest(
            @RequestBody PRCommentDTO dto,
            Principal principal) {

        if (userDetailsService.getUserRole(principal.getName()) == UserRole.SECURITY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        dto.setAuthorId(principal.getName());
        return passRequestCommentsService.addCommentToPassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявки по id
     * @param passRequestId заявки
     * @return заявка
     */
    @GetMapping("/get/{passRequestId}")
    public ResponseEntity<PassRequest> getPassRequestById(@PathVariable UUID passRequestId,
                                                          Principal principal) {

        if (userDetailsService.getUserRole(principal.getName()) == UserRole.SECURITY ||
                userDetailsService.getUserRole(principal.getName()) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestUserService.getPassRequestById(passRequestId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение количества заявок по статусу для пользователя
     * @param principal авторизация пользователя
     * @return заявки
     */
    @GetMapping("/count/get/user/status")
    public ResponseEntity<Map<PRStatus, Long>> getPassRequestCountByStatusForUser(
            Principal principal) {

        return passRequestUserService.getPassRequestCountByStatusForUser(principal.getName()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявок по статусу для пользователя
     * @param status заявки
     * @param page номер страницы
     * @param itemsPerPage размер страницы
     * @param userId идентификатор пользователя (опционально, добавлено для админа)
     * @param principal авторизация пользователя
     * @return заявки
     */
    @GetMapping("/get/user/status")
    public ResponseEntity<ResponseDTO<PassRequest>> getPassRequestByStatusForUser(
            @RequestParam Long page,
            @RequestParam Long itemsPerPage,
            @RequestParam String status,
            @RequestParam(required = false) String userId,
            Principal principal) {

        if (Optional.ofNullable(userId).isPresent()) {
            return passRequestUserService.getPassRequestByStatusForUser(userId, status, page, itemsPerPage).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return passRequestUserService.getPassRequestByStatusForUser(principal.getName(), status, page, itemsPerPage).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение количества заявок по статусу для админа ООВО
     * @param principal авторизация админа ООВО
     * @return мапа: статус - количество
     */
    @GetMapping("/count/get/admin/status")
    public ResponseEntity<Map<String, Integer>> getPassRequestCountByStatusForAdmin(
            Principal principal) {

        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return passRequestAdminService.getPassRequestsCountByStatusForAdmin(principal.getName()).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Получение заявок для обработки администратором ООВО
     * @param page номер страницы
     * @param pageSize размер страницы
     * @param status статус заявок
     * @param search поиск
     * @param principal авторизация пользователя
     * @return список заявок для обработки
     */
    @GetMapping("/get/requests")
    public ResponseEntity<ResponseDTO<PassRequest>> getPassRequestsForAdmin(
            @RequestParam(value = "page") Long page,
            @RequestParam(value = "itemsPerPage") Long pageSize,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "search", required = false) String search,
            Principal principal) {

        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return passRequestAdminService.getPassRequestsForAdmin(
                            status,
                            page,
                            pageSize,
                            search,
                            principal.getName()
                    )
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Получить список пользователей групповой заявки
     * @param dto заявки. Необходимо передать только id заявки
     * @return список пользователей заявки
     */
    @GetMapping("/get/users")
    public ResponseEntity<List<PassRequestUser>> getPassRequestUsers(@RequestBody PRDTO dto,
                                                                     Principal principal) {
        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return passRequestUserService.getPassRequestUsers(dto).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Получение комментариев заявки
     * @param passRequestId id заявки
     * @return список комментариев
     */
    @GetMapping("/comments/{passRequestId}")
    public ResponseEntity<List<PassRequestComment>> getCommentsByPassRequest(@PathVariable UUID passRequestId,
                                                                             Principal principal) {
        if (userDetailsService.getUserRole(principal.getName()) == UserRole.SECURITY ||
                userDetailsService.getUserRole(principal.getName()) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestCommentsService.getPassRequestComments(passRequestId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Редактирование статуса заявки
     * @param dto DTO заявки
     * @return отредактированная заявка
     */
    @PutMapping("/edit/status")
    public ResponseEntity<PassRequest> editPassRequestStatus(@RequestBody PRDTO dto,
                                                             Principal principal) {
        if (userDetailsService.getUserRole(principal.getName()) == UserRole.SECURITY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestAdminService.updatePassRequestStatus(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Редактирование дат действия заявки
     * @param dto заявки
     * @param principal атворизация пользователя
     * @return обновлённая заявка
     */
    @PutMapping("/edit/date")
    public ResponseEntity<PassRequest> editPassRequestDates(@RequestBody PRDTO dto,
                                                            Principal principal) {
        if (userDetailsService.getUserRole(principal.getName()) == UserRole.SECURITY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return passRequestAdminService.updatePassRequestDates(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Редактирование комментария
     * @param dto комментария с id
     * @return отредактированный комментарий
     */
    @PutMapping("/edit/comments")
    public ResponseEntity<PassRequestComment> editPassRequestComment(@RequestBody PRCommentDTO dto,
                                                                     Principal principal) {
        if (userDetailsService.getUserRole(principal.getName()) == UserRole.SECURITY ||
                userDetailsService.getUserRole(principal.getName()) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestCommentsService.updateComment(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление заявки по id
     * @param id заявки
     * @return статус OK (временное решение до spring security)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PassRequest> deletePassRequestById(@PathVariable UUID id,
                                                             Principal principal) {
        if (userDetailsService.isSecurityOfficer(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestUserService.deletePassRequestById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление пользователя из заявки
     * @param dto пользователя в заявке
     * @return обновлённый список пользоватлей заявки
     */
    @DeleteMapping("/delete_user")
    public ResponseEntity<List<PassRequestUser>> deleteUserFromPassRequest(@RequestBody PRUserDTO[] dto,
                                                                           Principal principal) {
        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return passRequestAdminService.deleteUserFromPassRequest(dto).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Удаление комментария
     * @param dto комментария с id
     * @return удалённый комментарий
     */
    @DeleteMapping("/comments/delete")
    public ResponseEntity<PassRequestComment> deletePassRequestComment(@RequestBody PRCommentDTO dto,
                                                                       Principal principal) {
        if (userDetailsService.isUniversity(principal.getName())
                || userDetailsService.isSuperUser(principal.getName())) {
            return passRequestCommentsService.deletePassRequestComment(dto).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
