package gisscos.studentcard.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

/**
 * Сущность, описывающая пользователя, указанного в групповой заявке
 */
@Data
@Entity
@NoArgsConstructor
public class PassRequestUser {

    /** Id пользователя, прикрепленного к заявке. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private UUID id;
    /** Заявка, к которой прикреплен пользователь (в таблице хранится только её id) */
    private UUID passRequestId;
    /** Id пользователя, прикрепленного к заявке */
    private String userId;

    public PassRequestUser(UUID passRequestId, String userId) {
        this.passRequestId = passRequestId;
        this.userId = userId;
    }
}
