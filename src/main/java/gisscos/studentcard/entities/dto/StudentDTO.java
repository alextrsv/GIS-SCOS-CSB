package gisscos.studentcard.entities.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO extends UserDTO {

    String id;
    String surname;
    String name;
    String middle_name;
    String snils;
    String inn;
    String email;
    String external_id;
    String scos_id;
    Integer study_year;
    String organization_id;
    List<StudyPlanDTO> study_plans;



    public StudentDTO(String id, String surname, String name, String middle_name,
                   String snils, String inn, String email, String external_id,
                   String scos_id, Integer study_year, String organization_id) {
        this.id = id;
        this.surname = surname;
        this.name = name;
        this.middle_name = middle_name;
        this.snils = snils;
        this.inn = inn;
        this.email = email;
        this.external_id = external_id;
        this.scos_id = scos_id;
        this.study_year = study_year;
        this.organization_id = organization_id.toString();

    }
}
