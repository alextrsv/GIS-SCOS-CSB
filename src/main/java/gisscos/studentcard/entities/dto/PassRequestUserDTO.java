package gisscos.studentcard.entities.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Pass request user data transfer object
 */
@Data
public class PassRequestUserDTO {
    private UUID userId;
    private Long passRequestId;
}
