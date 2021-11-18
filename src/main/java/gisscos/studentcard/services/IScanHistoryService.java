package gisscos.studentcard.services;

import gisscos.studentcard.entities.ScanHistory;
import gisscos.studentcard.entities.dto.ScanHistoriesWithPayloadDTO;
import gisscos.studentcard.entities.enums.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IScanHistoryService {
    Optional<ScanHistory> saveNewScanInHistory(UUID userId, UUID securityId, UserRole role);
    Optional<ScanHistoriesWithPayloadDTO> getScanHistoriesBySecurityId(UUID securityId, Pageable pageable);
}
