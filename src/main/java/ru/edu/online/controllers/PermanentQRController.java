package ru.edu.online.controllers;

import ru.edu.online.entities.enums.QRDataVerifyStatus;
import ru.edu.online.services.IPermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * контроллер для работы со статическими QR-кодами
 */
@RestController
@RequestMapping("permanent-qr/")
public class PermanentQRController {

    private final IPermanentQRService permanentQRService;

    @Autowired
    public PermanentQRController(IPermanentQRService permanentQRService) {
        this.permanentQRService = permanentQRService;
    }

    @PostMapping("hash")
    public ResponseEntity<QRDataVerifyStatus> verifyData(@RequestParam String userId,
                                                         @RequestParam String dataHash){
        return permanentQRService.verifyData(userId, dataHash).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("download/{id}")
    public ResponseEntity<Resource> downloadQrAsFile(@PathVariable String id){
        return permanentQRService.downloadQRAsFile(id).map(resource ->
                        ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_PNG)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "got permanent QR code")
                                .body(resource))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
