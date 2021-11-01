package gisscos.studentcard.services;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.DynamicQRDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DynamicQRService {

    Optional<List<DynamicQR>> getInfo(UUID userId, UUID organizationId);

    ResponseEntity<Resource> downloadQRAsFile(UUID userId, UUID organizationId);

}
