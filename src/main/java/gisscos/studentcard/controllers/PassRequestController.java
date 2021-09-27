package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.dto.PassRequestDTO;
import gisscos.studentcard.entities.dto.PassRequestUserDTO;
import gisscos.studentcard.services.PassRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы с заявками
 */
@RestController
@RequestMapping("/pass_requests")
public class PassRequestController {

    private final PassRequestService passRequestService;

    @Autowired
    public PassRequestController(PassRequestService passRequestService) {
        this.passRequestService = passRequestService;
    }

    /**
     * Создание новой заявки
     * @param passRequestDTO DTO заявки
     * @return созданная заявка
     */
    @PostMapping("/add")
    public ResponseEntity<PassRequest> addPassRequest(@RequestBody PassRequestDTO passRequestDTO) {
        return new ResponseEntity<>(passRequestService.createPassRequest(passRequestDTO), HttpStatus.CREATED);
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
     * Редактирование заявки
     * @param passRequestDTO DTO заявки
     * @return отредактированная заявка
     */
    @PutMapping("/edit")
    public ResponseEntity<PassRequest> editPassRequest(@RequestBody PassRequestDTO passRequestDTO) {
        return passRequestService.updatePassRequest(passRequestDTO).map(ResponseEntity::ok)
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

    @PostMapping("/addUser")
    public ResponseEntity<PassRequest> addUserToPassRequest(@RequestBody PassRequestUserDTO passRequestUserDTO) {
        return passRequestService.addUserToPassRequest(passRequestUserDTO).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
