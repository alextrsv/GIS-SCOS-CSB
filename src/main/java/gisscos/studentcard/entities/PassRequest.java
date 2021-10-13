package gisscos.studentcard.entities;

import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Класс, описывающий сущность заявки
 */
@Data                       // Генерация методов toString, equalsAndHashCode, getter и setter
@Entity
@NoArgsConstructor          // Генерация конструтора без параметров
public class PassRequest {

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

    /** Список пользователей групповой заявки */
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "passRequestId"
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @ToString.Exclude
    private List<PassRequestUser> users;

    /** Список файлов, прикрепленных к заявке */
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "passRequestId"
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @ToString.Exclude
    private List<PassFile> files;

    /** Список комментариев, прикрепленных к заявке */
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "passRequestId"
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @ToString.Exclude
    private List<PassRequestComment> comments;

    public PassRequest(Long userId, Long targetUniversityId, Long universityId,
                       LocalDate startDate, LocalDate endDate, PassRequestStatus status,
                       PassRequestType type) {
        this.creationDate = LocalDate.now();
        this.userId = userId;
        this.targetUniversityId = targetUniversityId;
        this.universityId = universityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.type = type;
    }
}
