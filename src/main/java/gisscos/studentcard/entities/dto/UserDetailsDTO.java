package gisscos.studentcard.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * dto - информации о пользователе по запросу на:
 * https://dev.online.edu.ru/api/v2/users/{user_id}
 * в UserDetailsService
 */
@Data
public class UserDetailsDTO {
    private UUID userId;
    private UUID esiaId;
    private String email;
    private String lastName;
    private String firstName;
    private String[] roles;
}
