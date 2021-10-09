package gisscos.studentcard.services;

import gisscos.studentcard.entities.DynamicQR;
import gisscos.studentcard.entities.dto.DynamicQRDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface DynamicQRService {

    Optional<DynamicQR> getInfo(String userToken);

    ResponseEntity<Resource> downloadQRAsFile(String userToken);

    Optional<DynamicQR> editPermanentQR(DynamicQRDTO dynamicQRDTO);

}
