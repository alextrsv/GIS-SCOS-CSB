package gisscos.studentcard.controllers;

import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permanent_qr_codes")
public class PermanentQRController {
    private final PermanentQRService permanentQRService;
@Autowired
    public PermanentQRController(PermanentQRService permanentQRService) {
        this.permanentQRService = permanentQRService;
    }
}
