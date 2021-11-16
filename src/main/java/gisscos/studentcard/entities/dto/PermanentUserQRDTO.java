package gisscos.studentcard.entities.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PermanentUserQRDTO {
    String userId;
    String surname;
    String name;
    String middle_name;
    String organization;
    String status;
    String role;
    List<OrganizationInQRDTO> accessed_organizations;
}
