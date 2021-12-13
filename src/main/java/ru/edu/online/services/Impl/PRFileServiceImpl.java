package ru.edu.online.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.PassRequestFile;
import ru.edu.online.entities.dto.PRFileIdentifierDTO;
import ru.edu.online.entities.enums.PRFileType;
import ru.edu.online.repositories.IPRFileRepository;
import ru.edu.online.repositories.IPRRepository;
import ru.edu.online.services.IPRFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Сервис для работы с файлами
 */
@Service
@Slf4j
public class PRFileServiceImpl implements IPRFileService {

    /** Репозиторий файлов */
    private final IPRFileRepository passFileRepository;

    private final IPRRepository passRequestRepository;

    /** Директория для хранения файлов. Указывается в application.properties */
    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    public PRFileServiceImpl(IPRFileRepository passFileRepository,
                             IPRRepository passRequestRepository) {
        this.passFileRepository = passFileRepository;
        this.passRequestRepository = passRequestRepository;
    }

    //TODO добавить работу с облачным хранилищем, если такое будет использоваться

    /**
     * Загрузка файла.
     * @param file файл
     * @param passRequestId идентификатор заявки
     * @return информация о загруженном файле
     */
    @Override
    public Optional<PassRequestFile> uploadPassFile(MultipartFile file, UUID passRequestId) {
        PassRequestFile passRequestFile;

        String path = System.getProperty("user.dir")
                + File.separator
                + uploadDir
                + File.separator
                + file.getOriginalFilename();

        log.debug("new file's path: " + path);
        System.out.println(File.separator);


        Optional<PassRequest> currentRequest =
                passRequestRepository.findById(passRequestId);
        if (currentRequest.isPresent()) {
            passRequestFile = new PassRequestFile(
                    file.getOriginalFilename(),
                    PRFileType.of(getFileExtension(file)),
                    path,
                    passRequestId
            );
        }
        else return Optional.empty();
        if(writeFileToDisk(file, path)) {
            log.info("new file \""
                    + file.getOriginalFilename()
                    + "\" has been uploaded");

            return Optional.of(passFileRepository.save(Objects.requireNonNull(passRequestFile)));
        }else{
            log.warn("file hasn't been uploaded");
            return Optional.empty();
        }
    }


    private String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return "";
        // если в имени файла есть точка и она не является первым символом в названии файла
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".")+1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";
    }

    /**
     * Загрузка множества файлов.
     * @param passFiles массив файлов
     * @param passRequestId идентификатор заявки
     * @return список, с информацией о загруженных файлах
     */
    @Override
    public List<PassRequestFile> uploadPassFiles(MultipartFile[] passFiles, UUID passRequestId) {
        ArrayList<PassRequestFile> uploadedFiles = new ArrayList<>();


        for (MultipartFile file: passFiles) {
            Optional<PassRequestFile> passFile = uploadPassFile(file, passRequestId);
            passFile.ifPresent(uploadedFiles::add);
        }
        log.info("files were uploaded");
        return uploadedFiles;
    }

    /**
     * Получить файл с сервера
//     * @param dto для поиска файла в репозитории
     * @return ResponseEntity, состоящее из описания файла, самого файла
     */
    @Override
    public ResponseEntity<Resource> downloadFile(UUID fileId, UUID passRequestId) {
        Optional<PassRequestFile> file = Optional.empty();

        Optional<PassRequest> passRequest =
                passRequestRepository.findById(passRequestId);
        if (passRequest.isPresent()) {
            file = passRequest.get().getFiles()
                    .stream()
                    .filter(f -> Objects.equals(f.getId(), fileId))
                    .findFirst();
        }

        if (file.isPresent()) {
            log.debug("find file with id {} is present", file.get().getId());
            try {
                Resource resource = new UrlResource(Path.of(file.get().getPath()).toUri());
                return ResponseEntity.ok()
                        .contentType(PRFileType.getMediaType(file.get().getType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + file.get().getName() + "\"")
                        .body(resource);
            } catch (IOException ex) {
                ex.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Получение информации о файле
     * @param dto для поиска файла в репозитории
     * @return информация о файле, если он найден в репозитории
     */
    @Override
    public Optional<PassRequestFile> getFile(PRFileIdentifierDTO dto) {
        log.info("loading info for file with {}", dto.getFileId());
        return passFileRepository.findById(dto.getFileId());
    }

    /**
     * Удаление файла
     * @param dto для поиска файла в репозитории
     * @return информация об удаленном файле
     */
    @Override
    public Optional<PassRequestFile> deletePassFile(PRFileIdentifierDTO dto) {
        Optional<PassRequestFile> passFile = getFile(dto);

        if (passFile.isPresent()){
            if(deleteFromDisk(passFile.get())) {
                passFileRepository.delete(passFile.get());
                log.info("file with id {} was deleted", dto.getFileId());
                return passFile;
            }
            log.error("file wasn't deleted from disk");
        }
        log.error("file wasn't found");
        return Optional.empty();
    }

    /**
     * Удаление файла с диска
     * @param passRequestFile информация о файле
     * @return был ли удален файл?
     */
    private boolean deleteFromDisk(PassRequestFile passRequestFile) {
        File fileToDelete = new File(passRequestFile.getPath());
        return fileToDelete.delete();
    }

    /**
     * Запись файла на диск
     * @param file файл
     * @param path путь для записи
     */
    private boolean writeFileToDisk(MultipartFile file, String path) {
        try {
            Files.copy(file.getInputStream(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
            log.info("file was written do disk");
            return true;
        } catch (IOException ex) {
            log.error("file wasn't written do disk due IOException");
            ex.printStackTrace();
            return false;
        }
    }
}
