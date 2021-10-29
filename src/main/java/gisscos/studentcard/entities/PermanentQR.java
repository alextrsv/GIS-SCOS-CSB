package gisscos.studentcard.entities;
import gisscos.studentcard.entities.enums.QRStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;



/**
 * Класс, описывающий сущность постоянного QR-кода
 */

@Data
@Entity
@NoArgsConstructor
@ToString
public class PermanentQR {

    /** Id кода в БД. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
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
}
