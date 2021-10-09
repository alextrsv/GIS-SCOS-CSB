package gisscos.studentcard.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
/**
 * Сущность, описывающая пользователя QR-кода
 */
@Data
@Entity
@NoArgsConstructor
public class PermanentQRUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** Статический QR-код */
    private Long permanentQRId;
    /** id пользователя */
    private Long userId;
    public PermanentQRUser (Long permanentQRId, Long userId) {
        this.permanentQRId = permanentQRId;
        this.userId = userId;
    }

}
