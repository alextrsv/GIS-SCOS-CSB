package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.DynamicQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDynamicQRRepository extends JpaRepository<DynamicQR, Long> {

    List<DynamicQR> getByUserId(UUID userId);

    List<DynamicQR> getByUserIdAndUniversityId(UUID userId, String OrganizationId);

    List<DynamicQR> getByUniversityId(String organizationId);
}
