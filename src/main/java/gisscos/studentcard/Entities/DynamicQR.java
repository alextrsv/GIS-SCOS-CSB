package gisscos.studentcard.Entities;
import gisscos.studentcard.Entities.Enums.QRStatus;
import gisscos.studentcard.Entities.Enums.QRType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
/**
 * Класс, описывающий сущность динамического QR-кода
 */
@Data
@Entity
@NoArgsConstructor
public class DynamicQR {
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
}
