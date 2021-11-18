package gisscos.studentcard.services;

import gisscos.studentcard.entities.enums.QRDataVerifyStatus;
import org.springframework.core.io.Resource;

import java.util.Optional;
import java.util.UUID;

public interface IPermanentQRService {

    Optional<Resource> downloadQRAsFile(UUID userId);

    Optional<QRDataVerifyStatus> verifyData(UUID userId, String dataHash);
}
