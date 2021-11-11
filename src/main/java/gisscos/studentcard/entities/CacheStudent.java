package gisscos.studentcard.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Сущность валидированного студента
 */
@Data
@Entity
@NoArgsConstructor
public class CacheStudent {

    /** Id валидированного студента в БД. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /** Идентификатор в СЦОСе */
    private UUID scosId;
    /** Дата валидации студента */
    private LocalDate validationDate;

    public CacheStudent(UUID scosId) {
        this.scosId = scosId;
        this.validationDate = LocalDate.now();
    }
}
