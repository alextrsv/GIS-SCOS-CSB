package gisscos.studentcard.entities.dto;

import lombok.Data;

/**
 * Pass request comment data transfer object
 */
@Data
public class PassRequestCommentDTO {
    private Long id;
    private Long authorId;
    private Long passRequestId;
    private String comment;
}
