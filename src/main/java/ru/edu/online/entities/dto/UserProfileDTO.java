package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.edu.online.entities.enums.UserRole;

/**
 * Профиль пользователя для фронта
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    private String organizationShortName;
    private String organizationFullName;
    private String firstName;
    private String lastName;
    private String patronymicName;
    private String email;
    private String studNumber;
    private String contingent_flow;
    private String flow_type;
    private String flow_date;
    private String faculty;
    private String education_form;
    private String form_fin;
    private String start_date;
    private String photoURL;
    private Integer studyYear;
    private UserRole role;
}
