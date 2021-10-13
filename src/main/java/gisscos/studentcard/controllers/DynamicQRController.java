package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.services.DynamicQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("dynamic-qr")
public class DynamicQRController {

    @Autowired
    DynamicQRService dynamicQRService;

    @GetMapping("/view")
    private ResponseEntity<DynamicQR> getInfo(@RequestHeader("Authorization") String userToken){
        return dynamicQRService.getInfo(userToken).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("download/")
    public ResponseEntity<Resource> downloadQrAsFile(@RequestHeader("Authorization") String userToken){
        return dynamicQRService.downloadQRAsFile(userToken);
    }

}
