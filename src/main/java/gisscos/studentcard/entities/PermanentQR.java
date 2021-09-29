package gisscos.studentcard.entities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gisscos.studentcard.entities.enums.QRStatus;
import lombok.*;
import org.springframework.data.annotation.Id;



import javax.persistence.*;
import java.util.HashMap;

/**
 * Класс, описывающий сущность постоянного QR-кода
 */

@Data
@Entity
@NoArgsConstructor
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
    private String data;

    public PermanentQR(Long userId, Long universityId, QRStatus status, String data) {
        this.userId = userId;
        this.universityId = universityId;
        this.status = status;
        this.data=data;
    }

    /*
    public String dataQR() {
        data = PermamentQR.super.toString();
        return data;
    }*/

    public String createQR(int width,int height,String format,String outPath) {
        HashMap ha = new HashMap();
        ha.put (EncodeHintType.CHARACTER_SET, "utf-8"); // Формат кодирования
        ha.put (EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // Уровень коррекции]
        ha.put(EncodeHintType.MARGIN, 2);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE,300 , 300, ha);
            //Path file = new File(outPath).toPath();
            //MatrixToImageWriter.writeToPath(bitMatrix, format, file);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
        return "SUCCESS";
    }
    @Override
    public String toString() {
        return super.toString() + "UserId:" + userId + "\n" + "UniversityId" + universityId + "\n" + "status" + status + "\n";
    }


}
