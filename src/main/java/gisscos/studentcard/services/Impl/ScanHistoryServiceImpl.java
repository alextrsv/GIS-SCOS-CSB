package gisscos.studentcard.services.Impl;

import gisscos.studentcard.entities.ScanHistory;
import gisscos.studentcard.entities.dto.ScanHistoriesWithPayloadDTO;
import gisscos.studentcard.entities.enums.UserRole;
import gisscos.studentcard.repositories.IScanHistoryRepository;
import gisscos.studentcard.services.IScanHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScanHistoryServiceImpl implements IScanHistoryService {

    private final IScanHistoryRepository scanHistoryRepository;

    @Autowired
    public ScanHistoryServiceImpl(IScanHistoryRepository scanHistoryRepository) {
        this.scanHistoryRepository = scanHistoryRepository;
    }

    @Override
    public Optional<ScanHistory> saveNewScanInHistory(UUID userId, UUID securityId, UserRole role) {
        ScanHistory sh = scanHistoryRepository
                .save(new ScanHistory(
                        userId,
                        securityId,
                        new Timestamp(new Date().getTime()),
                        role
                ));

        return sh == null? Optional.empty() : Optional.of(sh);
    }

    @Override
    public Optional<ScanHistoriesWithPayloadDTO> getScanHistoriesBySecurityId(UUID securityId, Pageable pageable) {
        Page<ScanHistory> scanHistoriesPage = scanHistoryRepository.findScanHistoriesBySecurityId(securityId, pageable);

        if (scanHistoriesPage.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ScanHistoriesWithPayloadDTO(scanHistoriesPage.getContent(), scanHistoriesPage.getTotalPages()));
    }
}
