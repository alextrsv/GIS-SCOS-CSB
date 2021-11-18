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

/**
 * Класс, описывающий сущность записи в жернале изменений заявки
 */
@Data
@Entity
@NoArgsConstructor
public class PassRequestChangeLogEntry {

    /** Id файла, прикрепленного к заявке в БД. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** Идентификатор заявки, которая была изменена */
    private Long passRequestId;
    /** Дата изменения */
    private LocalDate date;
    /** Параметр, который был изменён */
    private String parameter;
    /** Старое значение */
    private String oldValue;
    /** Новое значение */
    private String newValue;

    public PassRequestChangeLogEntry(String parameter, String oldValue,
                                     String newValue, Long passRequestId) {
        this.date = LocalDate.now();
        this.parameter = parameter;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.passRequestId = passRequestId;
    }
}