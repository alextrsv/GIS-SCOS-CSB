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

    /**
     * Загрузка прикреплённого к заявке файла на сервер.
     * @param passFile файл
     * @param passRequestId идентификатор заявки
     * @return загруженный файл если успешно
     */
    @PostMapping("/upload")
    private ResponseEntity<PassFile> uploadPassFile(@RequestParam("file") MultipartFile passFile,
                                                    @RequestParam("passRequestId") Long passRequestId) {
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
    private ResponseEntity<List<PassFile>> uploadPassFiles(@RequestParam("file") MultipartFile[] passFiles,
                                                           @RequestParam("passRequestId") Long passRequestId) {

        List<PassFile> uploadedFiles = passFileService.uploadPassFiles(passFiles, passRequestId);
        if (uploadedFiles.isEmpty())
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<>(uploadedFiles, HttpStatus.OK);
    }

    /**
     * Загрузка файла с сервера.
     * @param dto для нахождения файла
     * @return файл, прикреплённый к заявке
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestBody PassRequestFileIdentifierDTO dto) {
        return passFileService.downloadFile(dto);
    }

    /**
     * Получение информации о файле, прикрепленном к заявке.
     * @param dto для нахождения файла
     * @return информация о файле
     */
    @GetMapping("/view")
    public ResponseEntity<PassFile> getFileInfo(@RequestBody PassRequestFileIdentifierDTO dto) {
        return passFileService.getFile(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Удаление файла с сервера.
     * @param dto для нахождения файла
     * @return информация об удаленном файле
     */
    @DeleteMapping("/delete")
    private ResponseEntity<PassFile> deletePassFile(@RequestBody PassRequestFileIdentifierDTO dto){
        return passFileService.deletePassFile(dto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}