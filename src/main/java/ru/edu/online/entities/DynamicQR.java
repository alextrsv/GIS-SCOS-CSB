package ru.edu.online.entities;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.edu.online.entities.enums.QRStatus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Класс, описывающий сущность динамического QR-кода
 */
@Data
@Entity
@NoArgsConstructor
public class DynamicQR {

    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue
    private UUID id;
    /** id владельца QR-кода */
    private String userId;
    /** id университета, к которому относится QR-код */
    private String universityId;
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


    public DynamicQR(String userId, String universityId, QRStatus status,
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

    public DynamicQR(String userId, String universityId, QRStatus status, String content) {
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
