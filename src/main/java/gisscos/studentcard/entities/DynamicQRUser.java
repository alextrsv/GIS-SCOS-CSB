package gisscos.studentcard.entities;


import gisscos.studentcard.entities.dto.StudentDTO;
import gisscos.studentcard.entities.dto.UserDTO;
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
    private String userId;
    /** id родного университета */
    private String organizationId;


    public DynamicQRUser(StudentDTO studentDTO) {
        this.userId = studentDTO.getId();
        this.organizationId = studentDTO.getOrganization_id();
    }

    public DynamicQRUser(UserDTO userDTO) {
        this.userId = userDTO.getUser_id();
        this.organizationId = userDTO.getUserOrganizationsId().get(0);
    }

    public DynamicQRUser(String userId, String organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }
}
