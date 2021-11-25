package ru.edu.online.services;

import org.springframework.core.io.Resource;
import ru.edu.online.entities.enums.QRDataVerifyStatus;

import java.util.Optional;

public interface IPermanentQRService {

    Optional<Resource> downloadQRAsFile(String userId);

    Optional<QRDataVerifyStatus> verifyData(String userId, String dataHash);
}
