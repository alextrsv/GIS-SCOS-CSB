package gisscos.studentcard.entities;
import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.entities.enums.QRType;
import lombok.*;
import org.springframework.data.annotation.Id;

import javax.persistence.*;
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
    /** тип QR-кода */
    private QRType type;
    /** статус QR-кода */
    private QRStatus status;

    public DynamicQR(Long userId, Long universityId, QRType type, QRStatus status) {
        this.userId = userId;
        this.universityId = universityId;
        this.type = type;
        this.status = status;

    }
}
