package gisscos.studentcard.entities;


import gisscos.studentcard.entities.dto.StudentDTO;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DynamicQRUser {
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** id юзера в СЦОС */
    private UUID userId;
    /** id родного университета */
    private String universityId;


    public DynamicQRUser(StudentDTO studentDTO) {
        this.userId = studentDTO.getId();
        this.universityId = studentDTO.getOrganization_id();
    }
}
