package ru.edu.online.services.Impl;

import ru.edu.online.entities.ScanHistory;
import ru.edu.online.entities.dto.ScanHistoriesWithPayloadDTO;
import ru.edu.online.entities.dto.ScanHistoryDTO;
import ru.edu.online.repositories.IScanHistoryRepository;
import ru.edu.online.services.IScanHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
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
    public Optional<ScanHistory> saveNewScanInHistory(UUID securityId, ScanHistoryDTO scanHistoryDTO) {
        ScanHistory sh = scanHistoryRepository
                .save(new ScanHistory(
                        scanHistoryDTO.getUserId(),
                        securityId,
                        new Timestamp(new Date().getTime()),
                        scanHistoryDTO.getRole(),
                        scanHistoryDTO.getFullName().toLowerCase(Locale.ROOT)
                ));

        return sh == null? Optional.empty() : Optional.of(sh);
    }

    @Override
    public Optional<ScanHistoriesWithPayloadDTO> getScanHistoriesBySecurityId(
            UUID securityId, Pageable pageable, String searchByFullName) {

        Page<ScanHistory> scanHistoriesPage;

        if(searchByFullName.length() == 0) {
            scanHistoriesPage = scanHistoryRepository.findScanHistoriesBySecurityId(securityId, pageable);
        }else {
            scanHistoriesPage = scanHistoryRepository.findScanHistoriesBySecurityIdAndFullNameIsLike(
                    securityId, pageable, "%" + searchByFullName.toLowerCase(Locale.ROOT) + "%");
        }

        if (scanHistoriesPage.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ScanHistoriesWithPayloadDTO(
                scanHistoriesPage.getContent(), scanHistoriesPage.getTotalPages())
        );
    }
}
