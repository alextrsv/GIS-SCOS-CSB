package ru.edu.online.services;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.edu.online.entities.PassFile;
import ru.edu.online.entities.dto.PassRequestFileIdentifierDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPassFileService {
    Optional<PassFile> uploadPassFile(MultipartFile file, UUID passRequestId);

    List<PassFile> uploadPassFiles(MultipartFile[] passFiles, UUID passRequestId);

    ResponseEntity<Resource> downloadFile(UUID fileId, UUID passRequestId);

    Optional<PassFile> getFile(PassRequestFileIdentifierDTO dto);

    Optional<PassFile> deletePassFile(PassRequestFileIdentifierDTO dto);
}

