package gisscos.studentcard.services;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface PermanentQRService {

    ResponseEntity<Resource> downloadQRAsFile(UUID userId);
}
