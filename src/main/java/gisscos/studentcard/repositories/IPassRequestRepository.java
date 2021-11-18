package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPassRequestRepository extends JpaRepository<PassRequest, Long> {

    List<PassRequest> findAllByUserId(String userId);

    List<PassRequest> findAllByUniversityId(String universityId);

    List<PassRequest> findAllByTargetUniversityIdAndStatus(String targetUniversityId, PassRequestStatus status);

    List<PassRequest> findAllByTargetUniversityId(String targetUniversityId);

    List<PassRequest> findAllByStatus(PassRequestStatus status);

    Long countAllByNumberGreaterThan(Long number);
}
