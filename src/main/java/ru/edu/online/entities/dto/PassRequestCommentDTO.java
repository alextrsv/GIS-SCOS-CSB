package ru.edu.online.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Pass request comment data transfer object
 */
@Data
public class PassRequestCommentDTO {
    private UUID id;
    private String authorId;
    private UUID passRequestId;
    private String comment;

    public PassRequestCommentDTO(String authorId, UUID passRequestId, String comment) {
        this.authorId = authorId;
        this.passRequestId = passRequestId;
        this.comment = comment;
    }
}
