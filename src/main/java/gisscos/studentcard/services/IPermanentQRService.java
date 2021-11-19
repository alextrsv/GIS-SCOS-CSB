package gisscos.studentcard.services;

import gisscos.studentcard.entities.enums.QRDataVerifyStatus;
import org.springframework.core.io.Resource;

import java.util.Optional;

public interface IPermanentQRService {

    Optional<Resource> downloadQRAsFile(String userId);

    Optional<QRDataVerifyStatus> verifyData(String userId, String dataHash);
}
