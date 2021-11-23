package ru.edu.online.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.edu.online.entities.enums.UserRole;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "scan_history")
@NoArgsConstructor
public class ScanHistory {
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue
    @JsonIgnore
    private UUID id;
    /** id  охранника */
    @JsonIgnore
    private UUID securityId;
    /** id студента */
    private UUID userId;
    /** дата сканирования  */
    private Timestamp creationDate;
    /**роль пользователя */
    private UserRole role;
    /**ФИО пользователя */
    private String fullName;

    public ScanHistory(UUID userId, UUID securityId, Timestamp creationDate, UserRole role, String fullName){
        this.userId = userId;
        this.securityId = securityId;
        this.creationDate = creationDate;
        this.role = role;
        this.fullName = fullName;
    }
}
