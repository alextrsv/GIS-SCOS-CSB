package gisscos.studentcard.services;

import gisscos.studentcard.entities.DynamicQR;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDynamicQRService {

    Optional<List<DynamicQR>> getInfo(UUID userId, UUID organizationId);

    Optional<Resource> downloadQRAsFile(UUID userId, UUID organizationId);

    ResponseEntity<Resource> sendQRViaEmail(UUID userId, UUID organizationId);

}
