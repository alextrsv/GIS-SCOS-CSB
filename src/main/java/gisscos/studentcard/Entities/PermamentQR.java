package gisscos.studentcard.Entities;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gisscos.studentcard.Entities.Enums.QRStatus;
import gisscos.studentcard.Entities.Enums.QRType;
import lombok.*;
import org.springframework.data.annotation.Id;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import javax.persistence.*;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Класс, описывающий сущность постоянного QR-кода
 */

@Data
@Entity
@NoArgsConstructor
public class PermamentQR {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** id владельца QR-кода */
    private Long userId;
    /** id университета, к которому относится QR-код */
    private Long universityId;
    /** тип QR-кода */
    private QRType type;
    /** статус QR-кода */
    private QRStatus status;

    public PermamentQR(Long userId, Long universityId, QRType type, QRStatus status ) {
        this.userId = userId;
        this.universityId = universityId;
        this.type = type;
        this.status = status;
    }

    public static String createQR(int width,int height,String format,String outPath,String content) {
        HashMap ha = new HashMap();
        ha.put (EncodeHintType.CHARACTER_SET, "utf-8"); // Формат кодирования
        ha.put (EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // Уровень исправления ошибок [L, M, Q, H]
        ha.put(EncodeHintType.MARGIN, 2);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, ha);
            Path file = new File(outPath).toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, format, file);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
        return "SUCCESS";
    }


}
