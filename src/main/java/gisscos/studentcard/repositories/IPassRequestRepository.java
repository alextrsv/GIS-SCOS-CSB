package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPassRequestRepository extends JpaRepository<PassRequest, Long> {

    List<PassRequest> findAllByUserId(Long aLong);

    List<PassRequest> findAllByUniversityId(Long universityId);

    List<PassRequest> findAllByTargetUniversityIdAndStatus(Long targetUniversityId, PassRequestStatus status);

    List<PassRequest> findAllByStatus(PassRequestStatus status);
}
