package gisscos.studentcard.entities.dto;

import gisscos.studentcard.entities.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanHistoryDTO {
    private UUID userId;
    private UserRole role;
    private String fullName;
}
