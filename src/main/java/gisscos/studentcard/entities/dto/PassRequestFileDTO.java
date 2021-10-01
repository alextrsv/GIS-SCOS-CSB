package gisscos.studentcard.entities.dto;

import lombok.Data;

/**
 * Pass request file Data transfer object
 */
@Data
public class PassRequestFileDTO {
    private String name;
    private String type;
    private String path;
    private Long passRequestId;
}
