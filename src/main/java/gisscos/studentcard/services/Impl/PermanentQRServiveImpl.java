package gisscos.studentcard.services.Impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gisscos.studentcard.entities.PermanentQR;
import gisscos.studentcard.entities.dto.PermanentQRDTO;
import gisscos.studentcard.repositories.PermanentQRRepository;
import gisscos.studentcard.services.PermanentQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Optional;

/**
 * сервис для работы со статическими QR
 */
@Service
public class PermanentQRServiveImpl implements PermanentQRService {

    private final PermanentQRRepository permanentQRRepository;

@Autowired
    public PermanentQRServiveImpl(PermanentQRRepository permanentQRRepository) {
        this.permanentQRRepository = permanentQRRepository;
    }

    /**
     * генерация статического QR-кода
     * @param permanentQRDTO
     * @return qr
     */
    @Override
    public BitMatrix addPermanentQR(PermanentQRDTO permanentQRDTO) throws WriterException {
        PermanentQR permanentQR = new PermanentQR(permanentQRDTO.getUserId(), permanentQRDTO.getUniversityId(), permanentQRDTO.getStatus());
        HashMap ha = new HashMap();
        ha.put (EncodeHintType.CHARACTER_SET, "utf-8"); // Формат кодирования
        ha.put (EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // Уровень коррекции]
        ha.put(EncodeHintType.MARGIN, 2);
        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = permanentQR.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] thedigest = md.digest(bytesOfMessage);
        String content = null;
        try {
            content = new String(thedigest, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,300 , 300, ha);
            return bitMatrix;


    }

    /**
     * получение заявки по id
     * @param id
     * @return
     */
    @Override
    public Optional<PermanentQR> getPermanentQRById(Long id) {
        return permanentQRRepository.findById(id);
    }

    /**
     * редактирование QR-кода
     * @param permanentQRDTO
     * @return редактированный QR-код
     */
    @Override
    public Optional<PermanentQR> editPassRequest(PermanentQRDTO permanentQRDTO) {
        Optional<PermanentQR> permanentQR = permanentQRRepository.findById(permanentQRDTO.getId());
        if(permanentQR.isPresent()) {
            permanentQR.get().setStatus(permanentQRDTO.getStatus());
            permanentQR.get().setUniversityId(permanentQRDTO.getUniversityId());
            permanentQR.get().setUserId(permanentQRDTO.getUserId());
            return permanentQR;
        }
        else return Optional.empty();
    }

    /**
     * удаление QR-кода по id
     * @param id QR-кода
     * @return Удаленн
     */
    @Override
    public Optional<PermanentQR> deletePermanentQRById(Long id) {
        Optional<PermanentQR> permanentQR = permanentQRRepository.findById(id);
        if (permanentQR.isPresent()) {
            permanentQRRepository.deleteById(id);
            return permanentQR;
        }
        return Optional.empty();
    }

}
