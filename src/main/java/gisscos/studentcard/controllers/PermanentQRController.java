package gisscos.studentcard.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import gisscos.studentcard.entities.PermanentQR;
import gisscos.studentcard.entities.dto.PermanentQRDTO;
import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     *Генерация и добавление статического QR кода
     * @param permanentQRDTO
     * @return
     * @throws WriterException
     */
    @PostMapping("/add")
    public ResponseEntity<BitMatrix> addPermanentQR(@RequestBody PermanentQRDTO permanentQRDTO) throws WriterException {
    return new ResponseEntity<>(permanentQRService.addPermanentQR(permanentQRDTO), HttpStatus.CREATED);
    }

    /**
     * Получение статического QR кода по id
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<PermanentQR> getPermanentQRById(@PathVariable Long id) {
        return permanentQRService.getPermanentQRById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Редактирование статического QR-кода
     * @param permanentQRDTO
     * @return
     */
    @PutMapping("/edit")
    public ResponseEntity<PermanentQR> editPermanentQR(@RequestBody PermanentQRDTO permanentQRDTO) {
        return permanentQRService.editPermanentQR(permanentQRDTO).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
