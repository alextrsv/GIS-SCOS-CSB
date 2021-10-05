package gisscos.studentcard.services;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import gisscos.studentcard.entities.PermanentQR;
import gisscos.studentcard.entities.dto.PermanentQRDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

public interface PermanentQRService {

    BitMatrix addPermanentQR(PermanentQRDTO permanentQRDTO) throws WriterException;

    Optional<PermanentQR> getPermanentQRById(Long id);

    Optional<PermanentQR> editPermanentQR(PermanentQRDTO permanentQRDTO);

    Optional<PermanentQR> deletePermanentQRById(Long id);
}
