package gisscos.studentcard.entities.dto;

import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Pass request Data transfer object
 */
@Data
public class PassRequestDTO {
    private UUID id;
    private String userId;
    private String universityId;
    private String targetUniversityId;
    private String universityName;
    private String targetUniversityName;
    private String targetUniversityAddress;
    private LocalDate creationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private PassRequestType type;
    private PassRequestStatus status;
    private List<PassRequestUserDTO> users;
}
