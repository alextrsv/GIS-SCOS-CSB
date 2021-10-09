package gisscos.studentcard.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import gisscos.studentcard.entities.PermanentQR;
import gisscos.studentcard.entities.dto.PermanentQRDTO;
import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("download/")
    public ResponseEntity<Resource> downloadQrAsFile(@RequestHeader("Authorization") String userToken){
        return permanentQRService.downloadQRAsFile(userToken);
    }
}
