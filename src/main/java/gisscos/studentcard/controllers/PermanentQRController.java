package gisscos.studentcard.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import gisscos.studentcard.entities.PermanentQR;
import gisscos.studentcard.entities.dto.PermanentQRDTO;
import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * контроллер для работы со статическими QR-кодами
 */
@RestController
@RequestMapping("/permanent_qr_codes")
public class PermanentQRController {
    private final PermanentQRService permanentQRService;
@Autowired
    public PermanentQRController(PermanentQRService permanentQRService) {
        this.permanentQRService = permanentQRService;
    }

    @PostMapping("/add")
    public ResponseEntity<BitMatrix> addPermanentQR(@RequestBody PermanentQRDTO permanentQRDTO) throws WriterException {
    return new ResponseEntity<>(permanentQRService.addPermanentQR(permanentQRDTO), HttpStatus.CREATED);
    }
}
