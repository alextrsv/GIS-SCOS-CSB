package gisscos.studentcard.entities;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.entities.enums.QRType;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

/**
 * Класс, описывающий сущность динамического QR-кода
 */
@Data
@Entity
@NoArgsConstructor
public class DynamicQR {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** id владельца QR-кода */
    private Long userId;
    /** id университета, к которому относится QR-код */
    private Long universityId;
    /** статус QR-кода */
    private QRStatus status;
    /** Дата создание QR-кода */
    private LocalDate creationDate;
    /** Дата конца периода действия QR-кода */
    private LocalDate endDate;
    /** кодировка */
    private String characterSet;
    /** уровень коррекции */
    private ErrorCorrectionLevel errorCorrectionLevel;
    /** отступы */
    private int margin;
    /** Содержимое */
    String content;


    public DynamicQR(Long userId, Long universityId, QRStatus status,
                     String characterSet, ErrorCorrectionLevel errorCorrectionLevel, int margin) {
        this.creationDate = LocalDate.now();
        this.endDate = creationDate.plusDays(1);
        this.userId = userId;
        this.universityId = universityId;
        this.status = status;
        this.characterSet = characterSet;
        this.errorCorrectionLevel = errorCorrectionLevel;
        this.margin = margin;
    }

    public DynamicQR(Long userId, Long universityId, QRStatus status, String content) {
        this.creationDate = LocalDate.now();
        this.endDate = creationDate.plusDays(1);
        this.userId = userId;
        this.universityId = universityId;
        this.status = status;
        this.characterSet = "utf-8";
        this.errorCorrectionLevel = ErrorCorrectionLevel.M;
        this.margin = 2;
    }
}
