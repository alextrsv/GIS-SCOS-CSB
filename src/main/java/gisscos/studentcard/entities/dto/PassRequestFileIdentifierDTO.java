package gisscos.studentcard.entities.dto;

import lombok.Data;

/**
 * Pass request file identifier Data transfer object
 */
@Data
public class PassRequestFileIdentifierDTO {
    private Long fileId;
    private Long passRequestId;
}
