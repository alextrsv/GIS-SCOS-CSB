package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.repositories.PassFileRepository;
import gisscos.studentcard.services.PassFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PassFileServiceImpl implements PassFileService {

    private final PassFileRepository passFileRepository;

    public PassFileServiceImpl(PassFileRepository passFileRepository) {
        this.passFileRepository = passFileRepository;
    }

    //TODO добавить работу с облачным хранилищем, если такое будет использоваться

    @Override
    public PassFile uploadPassFile(MultipartFile file) {
        PassFile passFile = null;
        try {
            passFile = new PassFile(file.getName(), file.getContentType(),
                                                file.getSize(), LocalDate.now(), file.getBytes());
            writeFileToDisk(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    private void writeFileToDisk(MultipartFile file){
        try {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(new File("\\resources\\img" + file.getName())));
            stream.write(bytes);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
