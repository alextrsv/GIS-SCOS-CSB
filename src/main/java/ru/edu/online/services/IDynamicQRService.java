package ru.edu.online.services;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import ru.edu.online.entities.DynamicQR;

import java.util.List;
import java.util.Optional;

public interface IDynamicQRService {

    Optional<List<DynamicQR>> getQRByUserAndOrganization(String userId, String organizationId);

    Optional<Resource> downloadQRAsFile(String userId, String organizationId);

    Optional<List<DynamicQR>> getAllPermittedQRsAsFile(String userId);

    ResponseEntity<Resource> sendQRViaEmail(String userId, String organizationId);

    Optional<List<String>> getQRsContentByOrganization(String organizationId);
}
