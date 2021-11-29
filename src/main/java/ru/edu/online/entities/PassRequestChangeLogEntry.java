package ru.edu.online.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

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
    @GeneratedValue
    private UUID id;
    /** Идентификатор заявки, которая была изменена */
    private UUID passRequestId;
    /** Дата изменения */
    private LocalDateTime date;
    /** Параметр, который был изменён */
    private String parameter;
    /** Старое значение */
    private String oldValue;
    /** Новое значение */
    private String newValue;

    public PassRequestChangeLogEntry(String parameter, String oldValue,
                                     String newValue, UUID passRequestId) {
        this.date = LocalDateTime.now();
        this.parameter = parameter;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.passRequestId = passRequestId;
    }
}
