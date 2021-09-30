package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.enums.PassFileType;
import gisscos.studentcard.repositories.PassFileRepository;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class PassFileServiceImpl implements PassFileService {

    @Autowired
    private PassFileRepository passFileRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    //TODO добавить работу с облачным хранилищем, если такое будет использоваться


    @Override
    public PassFile uploadPassFile(MultipartFile file) {
        PassFile passFile;

        String path = System.getProperty("user.dir") + File.separator + uploadDir + File.separator + file.getOriginalFilename();
        System.out.println(File.separator);


        passFile = new PassFile(file.getOriginalFilename(), PassFileType.of(file.getOriginalFilename().split("\\.")[1]), path);
        writeFileToDisk(file, path);

        return passFileRepository.save(Objects.requireNonNull(passFile));
    }

    @Override
    public List<PassFile> uploadPassFiles(MultipartFile[] passFiles) {
        ArrayList<PassFile> uploadedFiles = new ArrayList<>();

        for (MultipartFile file: passFiles) {
            uploadedFiles.add(uploadPassFile(file));
        }
        return uploadedFiles;
    }

    @Override
    public ResponseEntity<PassFile> deletePassFile(String fileName) {
        Optional<PassFile> passFile = passFileRepository.findByName(fileName);

        if (passFile.isPresent()){
            if(deleteFromDisk(passFile.get())) {
                passFileRepository.delete(passFile.get());
                return new ResponseEntity<PassFile>(HttpStatus.OK);
            }
            else return new ResponseEntity<PassFile>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else return new ResponseEntity<PassFile>(HttpStatus.NO_CONTENT);
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
                        new BufferedOutputStream(new FileOutputStream(
                                new File(path)));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
