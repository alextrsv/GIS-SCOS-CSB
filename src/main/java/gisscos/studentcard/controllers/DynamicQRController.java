package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.services.IDynamicQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("dynamic-qr")
public class DynamicQRController {

    private final IDynamicQRService dynamicQRService;

    @Autowired
    public DynamicQRController(IDynamicQRService dynamicQRService) {
        this.dynamicQRService = dynamicQRService;
    }

    @GetMapping("/view/{userId}")
    private ResponseEntity<List<DynamicQR>> getInfo(@PathVariable UUID userId,
                                                    @RequestParam(name = "organizationId") String organizationId){

        return dynamicQRService.getQRByUserAndOrganization(userId, organizationId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("download/{userId}")
    public ResponseEntity<Resource> downloadQRAsFile(@PathVariable UUID userId,
                                                     @RequestParam(name = "organizationId") String organizationId){

        return dynamicQRService.downloadQRAsFile(userId, organizationId).map(resource ->
                ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "got dynamic QR code")
                        .body(resource))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("permitted/{userId}")
    public ResponseEntity<List<DynamicQR>> downloadAllPermittedQRAsFile(@PathVariable UUID userId){

        return dynamicQRService.getAllPermittedQRsAsFile(userId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());

    }

    @GetMapping("email/{userId}")
    public ResponseEntity<Resource> sendViaEmail(@PathVariable UUID userId,
                                                     @RequestParam(name = "organizationId") String organizationId){
        return dynamicQRService.sendQRViaEmail(userId, organizationId);
    }


    @GetMapping("content/")
    public ResponseEntity<List<String>> downloadQRsContent(@RequestParam(name = "organizationId") String organizationId){
        return dynamicQRService.getQRsContentByOrganization(organizationId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
