package gisscos.studentcard.services;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface PermanentQRService {

//    BitMatrix addPermanentQR(PermanentQRDTO permanentQRDTO) throws WriterException;
//
//    Optional<PermanentQR> getPermanentQRById(Long id);
//
//    Optional<PermanentQR> editPermanentQR(PermanentQRDTO permanentQRDTO);
//
//    Optional<PermanentQR> deletePermanentQRById(Long id);

    ResponseEntity<Resource> downloadQrAsFile(String userToken);
}
