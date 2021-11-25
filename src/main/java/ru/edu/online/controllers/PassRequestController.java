package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestComment;
import ru.edu.online.entities.User;
import ru.edu.online.entities.dto.PassRequestCommentDTO;
import ru.edu.online.entities.dto.PassRequestDTO;
import ru.edu.online.entities.dto.PassRequestUserDTO;
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
        dto.setUserId(principal.getName());
        switch (userDetailsService.getUserRole(principal)) {
            case ADMIN:
                return new ResponseEntity<>(passRequestService.addPassRequest(dto), HttpStatus.CREATED);
            case TEACHER:
            case STUDENT:
                if (dto.getType() == PassRequestType.SINGLE) {
                    return new ResponseEntity<>(passRequestService.addPassRequest(dto), HttpStatus.CREATED);
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
    public ResponseEntity<List<User>> addUserToPassRequest(@RequestBody PassRequestUserDTO dto,
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
     * @return заявки
     */
    @GetMapping("/get/user/status")
    public ResponseEntity<List<PassRequest>> getPassRequestByStatusForUser(@RequestParam Long page,
                                                                           @RequestParam Long itemsPerPage,
                                                                           @RequestParam String status,
                                                                           Principal principal) {
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
     * Получение заявок для обработки администратором ООВО
     * @param targetUniversityId идентификатор ООВО
     * @param page номер страницы
     * @param status статус заявок
     * @return список заявок для обработки
     */
    @GetMapping("/get/requests")
    public ResponseEntity<List<PassRequest>> getPassRequestsForAdmin(
            @RequestParam(value = "targetUniversityId") String targetUniversityId,
            @RequestParam(value = "page") Long page,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "search", required = false) Optional<String> search,
            Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.getPassRequestsForAdmin(
                            RequestsStatusForAdmin.of(status),
                            targetUniversityId,
                            page,
                            search
                    )
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Получение количества заявок по статусу для администратора
     * @param principal авторизация админа
     * @return заявки
     */
    @GetMapping("/count/get/admin/status")
    public ResponseEntity<Map<String, Long>> getPassRequestCountByStatusForAdmin(Principal principal) {
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.getPassRequestCountByStatusForAdmin(principal.getName()).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Получить список пользователей групповой заявки
     * @param dto заявки. Необходимо передать только id заявки
     * @return список пользователей заявки
     */
    @GetMapping("/get/users")
    public ResponseEntity<List<User>> getPassRequestUsers(@RequestBody PassRequestDTO dto,
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
     * Получение просроченных заявок
     * @return список просроченных заявок, которые были удалены
     */
    @GetMapping("/get/expired_requests")
    public ResponseEntity<List<PassRequest>> getExpiredPassRequests() {
        return ResponseEntity.of(passRequestService.getExpiredPassRequests());
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
        if (userDetailsService.getUserRole(principal) == UserRole.ADMIN) {
            return passRequestService.updatePassRequestStatus(dto).map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
    public ResponseEntity<List<User>> deleteUserFromPassRequest(@RequestBody PassRequestUserDTO[] dto,
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
