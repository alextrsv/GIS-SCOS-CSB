package gisscos.studentcard.entities.dto;

import gisscos.studentcard.entities.enums.PassRequestStatus;
import gisscos.studentcard.entities.enums.PassRequestType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Pass request Data transfer object
 */
@Data
public class PassRequestDTO {
    private Long id;
    private Long userId;
    private Long universityId;
    private Long targetUniversityId;
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
