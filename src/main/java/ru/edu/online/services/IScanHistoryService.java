package ru.edu.online.services;

import org.springframework.data.domain.Pageable;
import ru.edu.online.entities.ScanHistory;
import ru.edu.online.entities.dto.ScanHistoriesWithPayloadDTO;
import ru.edu.online.entities.dto.ScanHistoryDTO;

import java.util.Optional;
import java.util.UUID;

public interface IScanHistoryService {
    Optional<ScanHistory> saveNewScanInHistory(UUID securityId, ScanHistoryDTO scanHistoryDTO);
    Optional<ScanHistoriesWithPayloadDTO> getScanHistoriesBySecurityId(
            UUID securityId, Pageable pageable, String searchByFullName);
}
