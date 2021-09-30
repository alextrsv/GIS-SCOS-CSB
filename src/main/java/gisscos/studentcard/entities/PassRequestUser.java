package gisscos.studentcard.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Сущность, описывающая пользователя, указанного в групповой заявке
 */
@Data
@Entity
@NoArgsConstructor
public class PassRequestUser {

    /** Id пользователя, прикрепленного к заявке. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** Заявка, к которой прикреплен пользователь (в таблице хранится только её id) */
    private Long passRequestId;
    /** Id пользователя, прикрепленного к заявке */
    private Long userId;

    public PassRequestUser(Long passRequestId, Long userId) {
        this.passRequestId = passRequestId;
        this.userId = userId;
    }
}
