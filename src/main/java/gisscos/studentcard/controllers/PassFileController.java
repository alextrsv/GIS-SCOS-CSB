package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер для работы с прикрепленными к заявкам файлами
 */
@RestController
public class PassFileController {

    private final PassFileService passFileService;

    @Autowired
    public PassFileController(PassFileService passFileService) {
        this.passFileService = passFileService;
    }


    @PostMapping("/file")
    private ResponseEntity<PassFile> uploadPassFile(@RequestParam("file") MultipartFile passFile){
        return new ResponseEntity<>(passFileService.uploadPassFile(passFile), HttpStatus.OK);
    }


    @PostMapping("/files")
    private ResponseEntity<List<PassFile>> uploadPassFiles(@RequestParam("file") MultipartFile[] passFiles){
        return new ResponseEntity<>(passFileService.uploadPassFiles(passFiles), HttpStatus.OK);
    }


    @DeleteMapping("/file/{file-name}")
    private ResponseEntity<PassFile> deletePassFile(@PathVariable("file-name") String fileName){
        return passFileService.deletePassFile(fileName);
    }

    @GetMapping("/file/{file-name}")
    private ResponseEntity<PassFile> downloadFile(@PathVariable("file-name") String fileName){
        return passFileService.getFile(fileName).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(path = "file/download/{file-name}")
    public ResponseEntity<Resource> download(@PathVariable("file-name") String fileName) throws IOException {
        return passFileService.downloadFile(fileName);
    }
}