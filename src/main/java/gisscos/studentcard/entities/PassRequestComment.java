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
 * Класс, описывающий сущность комментариев заявки
 */
@Data
@Entity
@NoArgsConstructor
public class PassRequestComment {

    /** Id комментария в БД. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private UUID id;
    /** Id пользоватлея - создателя */
    private Long authorId;
    /** Id заявки, к которой прикреплён комментарий */
    private UUID passRequestId;
    /** Комментарий */
    private String comment;
    /** Дата создания */
    private LocalDate creationDate;
    /** Дата редактирования */
    private LocalDate editDate;
}
