package gisscos.studentcard.controllers;

import gisscos.studentcard.services.IPermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("download/")
    public ResponseEntity<Resource> downloadQrAsFile(@RequestHeader("Authorization") String userToken){
        return permanentQRService.downloadQRAsFile(userToken);
    }
}
