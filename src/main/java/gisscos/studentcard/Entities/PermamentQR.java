package gisscos.studentcard.Entities;

import gisscos.studentcard.Enums.QRStatus;
import gisscos.studentcard.Enums.QRType;
import lombok.*;
import org.springframework.data.annotation.Id;

import javax.persistence.*;

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


}
