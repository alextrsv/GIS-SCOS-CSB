package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.edu.online.entities.dto.PermanentUserQRDTO;
import ru.edu.online.entities.enums.QRDataVerifyStatus;
import ru.edu.online.entities.enums.UserRole;
import ru.edu.online.services.IPermanentQRService;
import ru.edu.online.services.IUserDetailsService;

import java.security.Principal;
import java.util.Optional;

/**
 * контроллер для работы со статическими QR-кодами
 */
@RestController
@RequestMapping("/permanent-qr")
public class PermanentQRController {

    private final IPermanentQRService permanentQRService;
    private final IUserDetailsService userDetailsService;

    @Autowired
    public PermanentQRController(IPermanentQRService permanentQRService, IUserDetailsService userDetailsService) {
        this.permanentQRService = permanentQRService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/hash")
    public ResponseEntity<QRDataVerifyStatus> verifyData(@RequestParam String userId,
                                                         @RequestParam String dataHash){
        return permanentQRService.verifyData(userId, dataHash).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadQrAsFile(@PathVariable String id){
        return permanentQRService.downloadQRAsFile(id).map(resource ->
                        ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_PNG)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "got permanent QR code")
                                .body(resource))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }


    @GetMapping("/payload/{id}")
    public ResponseEntity<PermanentUserQRDTO> getScanningUserInfo(@PathVariable String id,
                                                                  Principal principal){

        Optional<PermanentUserQRDTO> result;

//        if (userDetailsService.getUserRole(principal.getName()) == UserRole.UNDEFINED)
//        {
//            result = permanentQRService.getAbbreviatedStaticQRPayload(id);
//        }
        result = permanentQRService.getFullUserInfo(id);

        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/payload/anonymous/{id}")
    public ResponseEntity<PermanentUserQRDTO> getScanningUserInfoAnonymous(@PathVariable String id){
        Optional<PermanentUserQRDTO> result;

        result = permanentQRService.getAbbreviatedStaticQRPayload(id);

        return result.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
