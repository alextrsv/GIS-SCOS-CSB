package gisscos.studentcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gisscos.studentcard.entities.enums.UserRole;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Table(name = "scan_history")
@NoArgsConstructor
public class ScanHistory {
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;
    /** id  охранника */
    @JsonIgnore
    private UUID securityId;
    /** id студента */
    private UUID userId;
    /** дата сканирования  */
    private Timestamp creationDate;
    /**роль пользователя */
    private UserRole role;

    public ScanHistory(UUID userId, UUID securityId, Timestamp creationDate, UserRole role){
        this.userId = userId;
        this.securityId = securityId;
        this.creationDate = creationDate;
        this.role = role;
    }
}