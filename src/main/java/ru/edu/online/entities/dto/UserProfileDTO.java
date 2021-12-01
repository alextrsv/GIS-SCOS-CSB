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
    private String educationForm;
    private String photoURL;
    private Integer studyYear;
    private UserRole role;

}
