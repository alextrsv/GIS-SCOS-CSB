package gisscos.studentcard.controllers;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}