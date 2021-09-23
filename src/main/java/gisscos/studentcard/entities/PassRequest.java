package gisscos.studentcard.entities;

import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
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
 * Класс, описывающий сущность заявки
 */
@Data                       // Генерация методов toString, equalsAndHashCode, getter и setter
@Entity
@NoArgsConstructor          // Генерация конструтора без параметров
public class PassRequest {

    //TODO Добавить поле, описывающее файл, прикрепленный к заявке пользователем

    /** Id заявки в БД. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** Id пользоватлея - создателя */
    private Long userId;
    /** Id организации, в которую необходим доступ */
    private Long universityId;
    /** Дата создания заявки */
    private @Setter(AccessLevel.PROTECTED) LocalDate creationDate;
    /** Дата начала периода действия заявки */
    private LocalDate startDate;
    /** Дата конца периода действия заявки */
    private LocalDate endDate;
    /** Тип заявки */
    private PassRequestType type;
    /** Статус заявки */
    private PassRequestStatus status;
    /** Комментарий создателя заявки */
    private String comment;

    public PassRequest(Long userId, Long universityId, LocalDate startDate,
                       LocalDate endDate, PassRequestStatus status, String comment) {
        this.creationDate = LocalDate.now();
        this.userId = userId;
        this.universityId = universityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.comment = comment;
    }
}
