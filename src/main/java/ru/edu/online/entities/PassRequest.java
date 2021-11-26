package ru.edu.online.entities;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.edu.online.entities.enums.PassRequestStatus;
import ru.edu.online.entities.enums.PassRequestType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Класс, описывающий сущность заявки
 */
@Data                       // Генерация методов toString, equalsAndHashCode, getter и setter
@Entity
@NoArgsConstructor          // Генерация конструтора без параметров
public class PassRequest {

    /** Id заявки в БД. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue
    private UUID id;
    /** Номер заявки для отображения и поиска по нему на фронте */
    private Long number;
    /** Id пользоватлея - создателя */
    private String authorId;
    /** Имя автора */
    private String authorFirstName;
    /** Фамилия автора */
    private String authorLastName;
    /** Отчество автора */
    private String authorPatronymicName;
    private String authorUniversityId;
    private String authorUniversityName;
    /** Id организации, в которую необходим доступ (целевая ООВО) */
    private String targetUniversityId;
    /** Название целевой ООВО */
    private String targetUniversityName;
    /** Адрес целевой ООВО */
    private String targetUniversityAddress;
    /** Дата создания заявки */
    @Setter(AccessLevel.PROTECTED)
    private LocalDate creationDate;
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
    private List<PassRequestUser> passRequestUsers;

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

    /** Журнал изменений заявки */
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "passRequestId"
    )
    @LazyCollection(LazyCollectionOption.FALSE)
    @ToString.Exclude
    private List<PassRequestChangeLogEntry> changeLog;

    public PassRequest(String authorId, String authorFirstName, String authorLastName,
                       String authorPatronymicName, String authorUniversityId, String authorUniversityName,
                       LocalDate startDate, LocalDate endDate, PassRequestStatus status,
                       PassRequestType type, String targetUniversityAddress,
                       String targetUniversityName, String targetUniversityId,
                       Long number) {
        this.creationDate = LocalDate.now();
        this.authorId = authorId;
        this.targetUniversityId = targetUniversityId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.type = type;
        this.targetUniversityAddress = targetUniversityAddress;
        this.targetUniversityName = targetUniversityName;
        this.number = number;
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
        this.authorPatronymicName = authorPatronymicName;
        this.authorUniversityId = authorUniversityId;
        this.authorUniversityName = authorUniversityName;
    }
}
