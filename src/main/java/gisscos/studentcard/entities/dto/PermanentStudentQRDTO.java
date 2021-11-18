package gisscos.studentcard.entities.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PermanentStudentQRDTO {
    String userId;
    String surname;
    String name;
    String middle_name;
    String organization;
    String status;
    String role;
    String stud_bilet;
    String education_form;
    String start_year;
    String stud_bilet_duration;
    List<OrganizationInQRDTO> accessed_organizations;
}

