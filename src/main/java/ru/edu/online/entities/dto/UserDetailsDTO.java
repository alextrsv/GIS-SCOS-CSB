package ru.edu.online.entities.dto;

import lombok.Data;

/**
 * dto - информации о пользователе по запросу на:
 * https://dev.online.edu.ru/api/v2/users/{user_id}
 * в UserDetailsService
 */
@Data
public class UserDetailsDTO {
    private String userId;
    private String email;
    private String lastName;
    private String firstName;
    private String patronymicName;
    private String[] roles;
}
