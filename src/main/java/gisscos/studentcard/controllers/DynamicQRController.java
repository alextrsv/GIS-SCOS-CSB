package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.services.DynamicQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("dynamic-qr")
public class DynamicQRController {

    private final DynamicQRService dynamicQRService;

    @Autowired
    public DynamicQRController(DynamicQRService dynamicQRService) {
        this.dynamicQRService = dynamicQRService;
    }

    @GetMapping("/view/{userId}")
    private ResponseEntity<List<DynamicQR>> getInfo(@PathVariable UUID userId,
                                                    @RequestParam(name = "organizationId") UUID organizationId){

        return dynamicQRService.getInfo(userId, organizationId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("download/{userId}")
    public ResponseEntity<Resource> downloadQrAsFile(@PathVariable UUID userId,
                                                     @RequestParam(name = "organizationId") UUID organizationId){
        return dynamicQRService.downloadQRAsFile(userId, organizationId);
    }

}
