package gisscos.studentcard.entities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gisscos.studentcard.entities.enums.QRStatus;
import lombok.*;


import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Класс, описывающий сущность постоянного QR-кода
 */

@Data
@Entity
@NoArgsConstructor
@ToString
public class PermanentQR {

    /** Id кода в БД. Генерируется автоматически */
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** id владельца QR-кода */
    private Long userId;
    /** id университета, к которому относится QR-код */
    private Long universityId;
    /** статус QR-кода */
    private QRStatus status;
    /** данные в строке для генерации QR-кода */


    public PermanentQR(Long userId, Long universityId, QRStatus status) {
        this.userId = userId;
        this.universityId = universityId;
        this.status = status;

    }

    public String createQR(String outPath, String studentInf) {
        HashMap ha = new HashMap();
        ha.put (EncodeHintType.CHARACTER_SET, "utf-8"); // Формат кодирования
        ha.put (EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // Уровень коррекции]
        ha.put(EncodeHintType.MARGIN, 2);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(studentInf, BarcodeFormat.QR_CODE,300 , 300, ha);
            //Path file = new File(outPath).toPath();
            //MatrixToImageWriter.writeToPath(bitMatrix, format, file);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
        return "SUCCESS";
    }
    /**
     * Хеширование строки информации для статического QR
     */
    public String StringToHex(PermanentQR permanentQR) throws UnsupportedEncodingException {
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
        String res = new String(thedigest, "UTF-8");
        return res;
    }


 /*   @Override
    public String toString() {
        return super.toString() + "UserId:" + userId + "\n" + "UniversityId" + universityId + "\n" + "status" + status + "\n";
    }*/


}
