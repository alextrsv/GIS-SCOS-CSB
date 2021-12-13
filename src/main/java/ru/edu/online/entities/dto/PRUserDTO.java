package ru.edu.online.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Pass request user data transfer object
 */
@Data
public class PRUserDTO {
    private UUID passRequestId;
    private String userId;
    private String scosId;
    private String firstName;
    private String lastName;
    private String patronymicName;
    private String photoUrl;
}
