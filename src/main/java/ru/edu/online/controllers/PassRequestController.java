package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestComment;
import ru.edu.online.entities.PassRequestUser;
import ru.edu.online.entities.dto.PassRequestCommentDTO;
import ru.edu.online.entities.dto.PassRequestDTO;
import ru.edu.online.entities.dto.PassRequestUserDTO;
import ru.edu.online.entities.dto.ResponseDTO;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.PassRequestType;
import ru.edu.online.entities.enums.RequestsStatusForAdmin;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IPassRequestCommentsService;
import ru.edu.online.services.IPassRequestService;
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
public class PassRequestController {

    private final IPassRequestService passRequestService;
    private final IPassRequestCommentsService passRequestCommentsService;
    private final IUserDetailsService userDetailsService;

    @Autowired
    public PassRequestController(IPassRequestService passRequestService,
                                 IPassRequestCommentsService passRequestCommentsService,
                                 IUserDetailsService userDetailsService) {
        this.passRequestService = passRequestService;
        this.passRequestCommentsService = passRequestCommentsService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Создание новой заявки
     * @param dto DTO заявки
     * @return созданная заявка
     */
    @PostMapping("/add")
    public ResponseEntity<PassRequest> addPassRequest(@RequestBody PassRequestDTO dto,
                                                      Principal principal) {
        switch (userDetailsService.getUserRole(principal)) {
            case ADMIN:
                if (dto.getType() == PassRequestType.GROUP) {
                    return passRequestService.addGroupPassRequest(dto, principal).map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build());
                }
            case STUDENT:
                if (dto.getType() == PassRequestType.SINGLE) {
                    return passRequestService.addSinglePassRequest(dto, principal).map(ResponseEntity::ok)
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
    public ResponseEntity<List<PassRequestUser>> addUserToPassRequest(@RequestBody PassRequestUserDTO dto,
                                                                      Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.addUserToPassRequest(dto).map(ResponseEntity::ok)
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
    public ResponseEntity<PassRequestComment> addCommentToPassRequest(@RequestBody PassRequestCommentDTO dto,
                                                                      Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY ||
            userDetailsService.getUserRole(principal) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestService.getPassRequestById(passRequestId, principal.getName()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение заявок по id пользователя
     * @param principal пользователь, создавший заявку
     * @return заявка
     */
    @GetMapping("/user/get")
    public ResponseEntity<List<PassRequest>> getPassRequestByUserId(Principal principal) {
        return passRequestService.getPassRequestsByUserId(principal.getName()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Получение заявок по статусу для университета
     * @param dto заявки
     * @param page номер страницы
     * @param pageSize размер страницы
     * @return заявки
     */
    @GetMapping("/get/university/status/{page}/{pageSize}")
    public ResponseEntity<List<PassRequest>> getPassRequestByStatus(@RequestBody PassRequestDTO dto,
                                                                    @PathVariable Long page,
                                                                    @PathVariable Long pageSize) {
        return passRequestService.getPassRequestByStatusForUniversity(dto, page, pageSize).map(ResponseEntity::ok)
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
    public ResponseEntity<ResponseDTO<PassRequest>> getPassRequestByStatusForUser(@RequestParam Long page,
                                                                                  @RequestParam Long itemsPerPage,
                                                                                  @RequestParam String status,
                                                                                  @RequestParam(required = false) String userId,
                                                                                  Principal principal) {
        if (Optional.ofNullable(userId).isPresent()) {
            return passRequestService.getPassRequestByStatusForUser(userId, status, page, itemsPerPage).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return passRequestService.getPassRequestByStatusForUser(principal.getName(), status, page, itemsPerPage).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение количества заявок по статусу для пользователя
     * @param principal авторизация пользователя
     * @return заявки
     */
    @GetMapping("/count/get/user/status")
    public ResponseEntity<Map<PassRequestStatus, Long>> getPassRequestCountByStatusForUser(Principal principal) {
        return passRequestService.getPassRequestCountByStatusForUser(principal.getName()).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Получение количества заявок по статусу для админа ООВО
     * @param principal авторизация админа ООВО
     * @return мапа: статус - количество
     */
    @GetMapping("/count/get/admin/status")
    public ResponseEntity<Map<PassRequestStatus, Integer>> getPassRequestCountByStatusForAdmin(Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.getPassRequestsCountByStatusForAdmin(principal).map(ResponseEntity::ok)
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
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.getPassRequestsForAdmin(
                            RequestsStatusForAdmin.of(status),
                            page,
                            pageSize,
                            search,
                            principal
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
    public ResponseEntity<List<PassRequestUser>> getPassRequestUsers(@RequestBody PassRequestDTO dto,
                                                                     Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.getPassRequestUsers(dto).map(ResponseEntity::ok)
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
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY ||
            userDetailsService.getUserRole(principal) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestCommentsService.getPassRequestComments(passRequestId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Редактирование заявки
     * @param dto DTO заявки
     * @return отредактированная заявка
     */
    @PutMapping("/edit")
    public ResponseEntity<PassRequest> editPassRequest(@RequestBody PassRequestDTO dto,
                                                       Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY ||
            userDetailsService.getUserRole(principal) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestService.updatePassRequest(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Редактирование статуса заявки
     * @param dto DTO заявки
     * @return отредактированная заявка
     */
    @PutMapping("/edit/status")
    public ResponseEntity<PassRequest> editPassRequestStatus(@RequestBody PassRequestDTO dto,
                                                             Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return passRequestService.updatePassRequestStatus(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Редактирование дат действия заявки
     * @param dto заявки
     * @param principal атворизация пользователя
     * @return обновлённая заявка
     */
    @PutMapping("/edit/date")
    public ResponseEntity<PassRequest> editPassRequestDates(@RequestBody PassRequestDTO dto,
                                                            Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return passRequestService.updatePassRequestDates(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Редактирование комментария
     * @param dto комментария с id
     * @return отредактированный комментарий
     */
    @PutMapping("/edit/comments")
    public ResponseEntity<PassRequestComment> editPassRequestComment(@RequestBody PassRequestCommentDTO dto,
                                                                     Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.SECURITY ||
            userDetailsService.getUserRole(principal) == UserRole.UNDEFINED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
    public ResponseEntity<PassRequest> deletePassRequestById(@PathVariable UUID id,
                                                             Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.deletePassRequestById(id).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Удаление пользователя из заявки
     * @param dto пользователя в заявке
     * @return удаленный пользователь, если таковой найден
     */
    @DeleteMapping("/delete_user")
    public ResponseEntity<List<PassRequestUser>> deleteUserFromPassRequest(@RequestBody PassRequestUserDTO[] dto,
                                                                           Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.deleteUserFromPassRequest(dto).map(ResponseEntity::ok)
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
    public ResponseEntity<PassRequestComment> deletePassRequestComment(@RequestBody PassRequestCommentDTO dto,
                                                                       Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestCommentsService.deletePassRequestComment(dto).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
