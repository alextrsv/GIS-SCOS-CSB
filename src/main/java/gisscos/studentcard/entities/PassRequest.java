package gisscos.studentcard.entities;

import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Setter(AccessLevel.PROTECTED) Long id;


    
    /** Id пользоватлея - создателя */
    private Long userId;
    /** Id организации, в которую необходим доступ (целевая ООВО)*/
    private Long targetUniversityId;
    /** Id организации пользователя */
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
    /** Список пользователей групповой заявки */
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            mappedBy = "passRequestId"
    )
    @ToString.Exclude
    private List<PassRequestUser> users;

    public PassRequest(Long userId, Long targetUniversityId, Long universityId,
                       LocalDate startDate, LocalDate endDate, PassRequestStatus status,
                       PassRequestType type, String comment) {
        this.creationDate = LocalDate.now();
        this.userId = userId;
        this.targetUniversityId = targetUniversityId;
        this.universityId = universityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.type = type;
        this.comment = comment;
    }
}
