package ru.edu.online.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Pass request file identifier Data transfer object
 */
@Data
public class PRFileIdentifierDTO {
    private UUID fileId;
    private UUID passRequestId;
}
