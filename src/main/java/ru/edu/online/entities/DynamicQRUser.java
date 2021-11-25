package ru.edu.online.entities;


import lombok.*;
import ru.edu.online.entities.dto.StudentDTO;
import ru.edu.online.entities.dto.UserDTO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DynamicQRUser {
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue
    private UUID id;

    /** id юзера в СЦОС */
    private String userId;
    /** id родного университета */
    private String organizationId;


    public DynamicQRUser(StudentDTO studentDTO) {
        this.userId = studentDTO.getScos_id();  // поменял с getId() на getScos_id()
        this.organizationId = studentDTO.getOrganization_id();
    }

    public DynamicQRUser(UserDTO userDTO) {
        this.userId = userDTO.getUser_id();
        if (userDTO.getEmployments().size() != 0)
            this.organizationId = userDTO.getOrganizationID();
        else this.organizationId = "";
    }

    public DynamicQRUser(String userId, String organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }
}
