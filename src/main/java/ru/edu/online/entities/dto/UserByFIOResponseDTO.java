package ru.edu.online.entities.dto;

import lombok.Data;

@Data
public class UserByFIOResponseDTO {
    private Long totalPages;
    private UserDTO[] data;
}
