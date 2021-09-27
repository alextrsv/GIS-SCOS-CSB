package gisscos.studentcard.entities.dto;

import lombok.Data;

/**
 * Pass request user data transfer object
 */
@Data
public class PassRequestUserDTO {
    private Long id;
    private Long userId;
    private Long passRequestId;
}
