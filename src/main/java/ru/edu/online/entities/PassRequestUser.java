package ru.edu.online.entities;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

/**
 * Сущность, описывающая пользователя, указанного в групповой заявке
 */
@Data
@Entity
@NoArgsConstructor
public class PassRequestUser {

    /** Id пользователя, прикрепленного к заявке. Генерируется автоматически */
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue
    private UUID id;
    /** Заявка, к которой прикреплен пользователь (в таблице хранится только её id) */
    private UUID passRequestId;
    /** СЦОС Id пользователя, прикрепленного к заявке */
    private String scosId;
    /** Имя */
    private String firstName;
    /** Фамилия */
    private String lastName;
    /** Отчество */
    private String patronymicName;
    /** Ссылка на фото */
    private String photoUrl;

    public PassRequestUser(UUID passRequestId, String scosId, String firstName,
                           String lastName, String patronymicName, String photoUrl) {
        this.passRequestId = passRequestId;
        this.scosId = scosId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymicName = patronymicName;
        this.photoUrl = photoUrl;
    }
}
