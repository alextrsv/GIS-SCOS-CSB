package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassRequestRepository extends JpaRepository<PassRequest, Long> {

    List<PassRequest> findAllByUniversityId(Long universityId);

    List<PassRequest> findAllByTargetUniversityId(Long targetUniversityId);
}
