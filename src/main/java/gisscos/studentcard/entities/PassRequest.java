package gisscos.studentcard.entities;

import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

/**
 * Класс, описывающий сущность заявки
 */
@Entity
public class PassRequest {

    //TODO Добавить поле, описывающее файл, прикрепленный к заявке пользователем

    /** Id заявки в БД. Генерируется автоматически */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /** Id пользоватлея - создателя */
    private Long userId;
    /** Id организации, в которую необходим доступ */
    private Long universityId;
    /** Дата создания заявки */
    private LocalDate creationDate;
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

    public PassRequest() {
    }

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

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setType(PassRequestType type) {
        this.type = type;
    }

    public void setStatus(PassRequestStatus status) {
        this.status = status;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getUniversityId() {
        return universityId;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public PassRequestType getType() {
        return type;
    }

    public PassRequestStatus getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }
}
