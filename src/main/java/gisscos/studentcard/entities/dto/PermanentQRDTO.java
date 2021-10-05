package gisscos.studentcard.entities.dto;

import gisscos.studentcard.entities.enums.QRStatus;
import lombok.Data;

@Data
public class PermanentQRDTO {
    private Long id;
    private Long userId;
    private Long universityId;
    private QRStatus status;
    private String studentInformation;
}
