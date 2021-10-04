package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.entities.dto.PassRequestFileIdentifierDTO;
import gisscos.studentcard.entities.enums.PassFileType;
import gisscos.studentcard.repositories.PassFileRepository;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Сервис для работы с файлами
 */
@Service
public class PassFileServiceImpl implements PassFileService {

    /** Репозиторий файлов */
    private final PassFileRepository passFileRepository;

    /** Директория для хранения файлов. Указывается в application.properties */
    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    public PassFileServiceImpl(PassFileRepository passFileRepository) {
        this.passFileRepository = passFileRepository;
    }

    //TODO добавить работу с облачным хранилищем, если такое будет использоваться

    /**
     * Загрузка файла.
     * @param file файл
     * @param passRequestId идентификатор заявки
     * @return информация о загруженном файле
     */
    @Override
    public PassFile uploadPassFile(MultipartFile file, Long passRequestId) {
        PassFile passFile;

        String path = System.getProperty("user.dir")
                + File.separator
                + uploadDir
                + File.separator
                + file.getOriginalFilename();

        System.out.println(File.separator);


        passFile = new PassFile(
                file.getOriginalFilename(),
                PassFileType.of(
                        Objects.requireNonNull(
                                file.getOriginalFilename()
                        ).split("\\.")[1]
                ),
                path,
                passRequestId
        );
        writeFileToDisk(file, path);

        return passFileRepository.save(Objects.requireNonNull(passFile));
    }

    /**
     * Загрузка множества файлов.
     * @param passFiles массив файлов
     * @param passRequestId идентификатор заявки
     * @return список, с информацией о загруженных файлах
     */
    @Override
    public List<PassFile> uploadPassFiles(MultipartFile[] passFiles, Long passRequestId) {
        ArrayList<PassFile> uploadedFiles = new ArrayList<>();

        for (MultipartFile file: passFiles) {
            uploadedFiles.add(uploadPassFile(file, passRequestId));
        }

        return uploadedFiles;
    }

    /**
     * Получить файл с сервера
     * @param dto для поиска файла в репозитории
     * @return ResponseEntity, состоящее из описания файла, самого файла
     */
    @Override
    public ResponseEntity<Resource> downloadFile(PassRequestFileIdentifierDTO dto) {
        Optional<PassFile> file = passFileRepository.findById(dto.getFileId());
        if (file.isPresent()) {
            try {
                Resource resource = new UrlResource(Path.of(file.get().getPath()).toUri());
                return ResponseEntity.ok()
                        .contentType(PassFileType.getMediaType(file.get().getType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.get().getName() + "\"")
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
    public Optional<PassFile> getFile(PassRequestFileIdentifierDTO dto) {
        return passFileRepository.findById(dto.getFileId());
    }

    /**
     * Удаление файла
     * @param dto для поиска файла в репозитории
     * @return информация об удаленном файле
     */
    @Override
    public Optional<PassFile> deletePassFile(PassRequestFileIdentifierDTO dto) {
        Optional<PassFile> passFile = getFile(dto);

        if (passFile.isPresent()){
            if(deleteFromDisk(passFile.get())) {
                passFileRepository.delete(passFile.get());
                return passFile;
            }
        }
        return Optional.empty();
    }

    /**
     * Удаление файла с диска
     * @param passFile информация о файле
     * @return был ли удален файл?
     */
    private boolean deleteFromDisk(PassFile passFile) {
        File fileToDelete = new File(passFile.getPath());
        return fileToDelete.delete();
    }

    /**
     * Запись файла на диск
     * @param file файл
     * @param path путь для записи
     */
    private void writeFileToDisk(MultipartFile file, String path) {
        try {
            Files.copy(file.getInputStream(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
