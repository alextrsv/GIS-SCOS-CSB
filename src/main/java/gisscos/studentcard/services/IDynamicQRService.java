package gisscos.studentcard.services;

import gisscos.studentcard.entities.DynamicQR;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDynamicQRService {

    Optional<List<DynamicQR>> getQRByUserAndOrganization(UUID userId, String organizationId);

    Optional<Resource> downloadQRAsFile(UUID userId, String organizationId);

    Optional<List<DynamicQR>> getAllPermittedQRsAsFile(UUID userId);

    ResponseEntity<Resource> sendQRViaEmail(UUID userId, String organizationId);

    Optional<List<String>> getQRsContentByOrganization(String organizationId);
}
