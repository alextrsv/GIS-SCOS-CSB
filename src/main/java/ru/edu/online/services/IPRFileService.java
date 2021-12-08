package ru.edu.online.services;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.edu.online.entities.PassRequestFile;
import ru.edu.online.entities.dto.PRFileIdentifierDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPRFileService {
    Optional<PassRequestFile> uploadPassFile(MultipartFile file, UUID passRequestId);

    List<PassRequestFile> uploadPassFiles(MultipartFile[] passFiles, UUID passRequestId);

    ResponseEntity<Resource> downloadFile(UUID fileId, UUID passRequestId);

    Optional<PassRequestFile> getFile(PRFileIdentifierDTO dto);

    Optional<PassRequestFile> deletePassFile(PRFileIdentifierDTO dto);
}

