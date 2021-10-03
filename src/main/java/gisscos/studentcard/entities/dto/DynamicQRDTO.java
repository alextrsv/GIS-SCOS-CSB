package gisscos.studentcard.entities.dto;

import gisscos.studentcard.entities.enums.QRStatus;
import gisscos.studentcard.entities.enums.QRType;
import lombok.Data;

@Data
public class DynamicQRDTO {
    private Long userId;
    private Long universityId;
    private QRType type;
    private QRStatus status;

}
