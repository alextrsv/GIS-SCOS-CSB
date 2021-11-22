package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.ScanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {
    Page<ScanHistory> findScanHistoriesBySecurityId(UUID securityId, Pageable pageable);
    Page<ScanHistory> findScanHistoriesBySecurityIdAndFullNameIsLike (UUID securityId, Pageable pageable, String fullName);
}
