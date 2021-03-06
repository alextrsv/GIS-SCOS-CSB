package ru.edu.online.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.edu.online.entities.PassRequestFile;
import ru.edu.online.entities.dto.PRFileIdentifierDTO;
import ru.edu.online.services.IPRFileService;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с прикрепленными к заявкам файлами
 */
@RestController
@RequestMapping("/file")
public class PRFileController {

    private final IPRFileService passFileService;

    @Autowired
    public PRFileController(IPRFileService passFileService) {
        this.passFileService = passFileService;
    }

    /**
     * Загрузка прикреплённого к заявке файла на сервер.
     * @param passFile файл
     * @param passRequestId идентификатор заявки
     * @return загруженный файл если успешно
     */
    @PostMapping("/upload")
    private ResponseEntity<PassRequestFile> uploadPassFile(@RequestParam("file") MultipartFile passFile,
                                                           @RequestParam("passRequestId") UUID passRequestId) {
        return passFileService.uploadPassFile(passFile, passRequestId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Загрузка прикреплённых к заявке файлов на сервер.
     * @param passFiles массив файлов
     * @param passRequestId идентификатор заявки
     * @return список загруженных файлов если успешно
     */
    @PostMapping("/upload/multiple")
    private ResponseEntity<List<PassRequestFile>> uploadPassFiles(@RequestParam("file") MultipartFile[] passFiles,
                                                                  @RequestParam("passRequestId") UUID passRequestId) {

        List<PassRequestFile> uploadedFiles = passFileService.uploadPassFiles(passFiles, passRequestId);
        if (uploadedFiles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>(uploadedFiles, HttpStatus.OK);
    }

    /**
     * Загрузка файла с сервера.
//     * @param dto для нахождения файла
     * @return файл, прикреплённый к заявке
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("fileId") UUID fileId,
                                             @RequestParam("passRequestId") UUID passRequestId) {
        return passFileService.downloadFile(fileId, passRequestId);
    }

    /**
     * Получение информации о файле, прикрепленном к заявке.
     * @param dto для нахождения файла
     * @return информация о файле
     */
    @GetMapping("/view")
    public ResponseEntity<PassRequestFile> getFileInfo(@RequestBody PRFileIdentifierDTO dto) {
        return passFileService.getFile(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление файла с сервера.
     * @param dto для нахождения файла
     * @return информация об удаленном файле
     */
    @DeleteMapping("/delete")
    private ResponseEntity<PassRequestFile> deletePassFile(@RequestBody PRFileIdentifierDTO dto){
        return passFileService.deletePassFile(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}