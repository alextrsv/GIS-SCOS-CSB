package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequest;
import gisscos.studentcard.entities.enums.PassRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassRequestRepository extends JpaRepository<PassRequest, Long> {

    List<PassRequest> findAllByUserId(Long aLong);

    List<PassRequest> findAllByUniversityId(Long universityId);

    List<PassRequest> findAllByTargetUniversityId(Long targetUniversityId);

    List<PassRequest> findAllByStatus(PassRequestStatus status);
}
