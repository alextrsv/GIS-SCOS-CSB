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
 * Класс, описывающий сущность комментариев заявки
 */
@Data
@Entity
@NoArgsConstructor
public class PassRequestComment {

    /** Id комментария в БД. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Setter(AccessLevel.PROTECTED) Long id;
    /** Id пользоватлея - создателя */
    private Long authorId;
    /** Id заявки, к которой прикреплён комментарий */
    private Long passRequestId;
    /** Комментарий */
    private String comment;
    /** Дата создания */
    private LocalDate creationDate;
    /** Дата редактирования */
    private LocalDate editDate;
}
