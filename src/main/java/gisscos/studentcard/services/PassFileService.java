package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

public interface PassFileService {
    PassFile uploadPassFile(MultipartFile file);
    List<PassFile> uploadPassFiles(MultipartFile[] passFiles);
    ResponseEntity<PassFile> deletePassFile(String fileName);
}

