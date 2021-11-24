package ru.edu.online.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.edu.online.entities.enums.UserRole;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanHistoryDTO {
    private UUID userId;
    private UserRole role;
    private String fullName;
}
