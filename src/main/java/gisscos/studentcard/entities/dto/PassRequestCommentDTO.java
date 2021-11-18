package gisscos.studentcard.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Pass request comment data transfer object
 */
@Data
public class PassRequestCommentDTO {
    private UUID id;
    private Long authorId;
    private UUID passRequestId;
    private String comment;
}
