package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.ScanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {
//    List<ScanHistory> getScanHistoriesBySecurityId(UUID securityId);
    Page<ScanHistory> findScanHistoriesBySecurityId(UUID securityId, Pageable pageable);

}
