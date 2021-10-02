package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.entities.dto.PassRequestFileIdentifierDTO;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Контроллер для работы с прикрепленными к заявкам файлами
 */
@RestController
@RequestMapping("/file")
public class PassFileController {

    private final PassFileService passFileService;

    @Autowired
    public PassFileController(PassFileService passFileService) {
        this.passFileService = passFileService;
    }

    @PostMapping("/upload")
    private ResponseEntity<PassFile> uploadPassFile(@RequestParam("file") MultipartFile passFile,
                                                    @RequestParam("passRequestId") Long passRequestId) {
        return new ResponseEntity<>(
                passFileService.uploadPassFile(passFile, passRequestId),
                HttpStatus.OK
        );
    }

    @PostMapping("/upload/multiple")
    private ResponseEntity<List<PassFile>> uploadPassFiles(@RequestParam("file") MultipartFile[] passFiles,
                                                           @RequestParam("passRequestId") Long passRequestId) {
        return new ResponseEntity<>(
                passFileService.uploadPassFiles(passFiles, passRequestId),
                HttpStatus.OK
        );
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestBody PassRequestFileIdentifierDTO dto) {
        return passFileService.downloadFile(dto);
    }

    @GetMapping("/view")
    public ResponseEntity<PassFile> getFileInfo(@RequestBody PassRequestFileIdentifierDTO dto) {
        return passFileService.getFile(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/delete")
    private ResponseEntity<PassFile> deletePassFile(@RequestBody PassRequestFileIdentifierDTO dto){
        return passFileService.deletePassFile(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}