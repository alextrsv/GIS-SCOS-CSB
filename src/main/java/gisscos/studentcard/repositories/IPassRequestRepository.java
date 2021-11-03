package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPassRequestRepository extends JpaRepository<PassRequest, Long> {

    List<PassRequest> findAllByUserId(Long aLong);

    List<PassRequest> findAllByUniversityId(UUID universityId);

    List<PassRequest> findAllByTargetUniversityId(UUID targetUniversityId);

    List<PassRequest> findAllByStatus(PassRequestStatus status);

    List<PassRequest> findAllByUserId(UUID userId);
}
