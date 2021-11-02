package gisscos.studentcard.services;

import org.springframework.core.io.Resource;

import java.util.Optional;
import java.util.UUID;

public interface PermanentQRService {

    Optional<Resource> downloadQRAsFile(UUID userId);
}
