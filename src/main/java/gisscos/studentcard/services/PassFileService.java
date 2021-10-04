package gisscos.studentcard.services;

import gisscos.studentcard.entities.PassFile;
import gisscos.studentcard.entities.dto.PassRequestFileIdentifierDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PassFileService {
    PassFile uploadPassFile(MultipartFile file, Long passRequestId);

    List<PassFile> uploadPassFiles(MultipartFile[] passFiles, Long passRequestId);

    ResponseEntity<Resource> downloadFile(PassRequestFileIdentifierDTO dto);

    Optional<PassFile> getFile(PassRequestFileIdentifierDTO dto);

    Optional<PassFile> deletePassFile(PassRequestFileIdentifierDTO dto);
}

