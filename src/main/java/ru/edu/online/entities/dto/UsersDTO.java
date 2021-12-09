package ru.edu.online.entities.dto;

import lombok.Data;

@Data
public class UsersDTO {
    private Long totalPages;
    private UserDTO[] data;
}
