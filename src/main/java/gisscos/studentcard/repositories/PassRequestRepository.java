package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassRequestRepository extends JpaRepository<PassRequest, Long> {
}
