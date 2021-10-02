package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.entities.dto.PassRequestFileIdentifierDTO;
import gisscos.studentcard.entities.enums.PassFileType;
import gisscos.studentcard.repositories.PassFileRepository;
import gisscos.studentcard.repositories.PassRequestRepository;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PassFileServiceImpl implements PassFileService {

    private final PassFileRepository passFileRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    public PassFileServiceImpl(PassFileRepository passFileRepository) {
        this.passFileRepository = passFileRepository;
    }

    //TODO добавить работу с облачным хранилищем, если такое будет использоваться

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

    @Override
    public List<PassFile> uploadPassFiles(MultipartFile[] passFiles, Long passRequestId) {
        ArrayList<PassFile> uploadedFiles = new ArrayList<>();

        for (MultipartFile file: passFiles) {
            uploadedFiles.add(uploadPassFile(file, passRequestId));
        }

        return uploadedFiles;
    }

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

    @Override
    public Optional<PassFile> getFile(String fileName) {
        return passFileRepository.findByName(fileName);
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String fileName) throws IOException {
        File file = new File(getFile(fileName).get().getPath());
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private boolean deleteFromDisk(PassFile passFile) {
        File fileToDelete = new File(passFile.getPath());
        return fileToDelete.delete();
    }


    private void writeFileToDisk(MultipartFile file, String path){
        if (file != null) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(path));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
