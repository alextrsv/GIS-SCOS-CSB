package gisscos.studentcard.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Pass request file identifier Data transfer object
 */
@Data
public class PassRequestFileIdentifierDTO {
    private UUID fileId;
    private UUID passRequestId;
}
