package gisscos.studentcard.controllers;

import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * контроллер для работы со статическими QR-кодами
 */
@RestController
@RequestMapping("permanent-qr/")
public class PermanentQRController {

    private final PermanentQRService permanentQRService;

    @Autowired
    public PermanentQRController(PermanentQRService permanentQRService) {
        this.permanentQRService = permanentQRService;
    }

    @GetMapping("download/{id}")
    public ResponseEntity<Resource> downloadQrAsFile(@PathVariable UUID id){
        return permanentQRService.downloadQRAsFile(id);
    }
}
