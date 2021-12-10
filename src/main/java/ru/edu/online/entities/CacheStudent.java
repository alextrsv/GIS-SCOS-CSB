package ru.edu.online.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

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
    /** Дата последней валидации студента */
    private LocalDate validationDate;
    /** Почта */
    private String email;
    /** Идентификатор в СЦОСе */
    private String scosId;
    /** Валиден ли кэш? */
    private boolean isValid;

    public CacheStudent(String email, String scosId) {
        this.validationDate = LocalDate.now();
        this.scosId = scosId;
        this.email = email;
        this.isValid = true;
    }

    public String getStudNumber() {
        return String.valueOf((int) (this.id + 27549369));
    }
}
