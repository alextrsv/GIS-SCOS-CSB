package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;

public interface PassFileService {
    PassFile uploadPassFile(MultipartFile file);
    List<PassFile> uploadPassFiles(MultipartFile[] passFiles);
    ResponseEntity<PassFile> deletePassFile(String fileName);

    Optional<PassFile> getFile(String fileName);

    ResponseEntity<Resource> downloadFile(String fileName) throws IOException;
}

