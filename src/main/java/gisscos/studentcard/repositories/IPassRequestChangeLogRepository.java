package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequestChangeLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPassRequestChangeLogRepository extends JpaRepository<PassRequestChangeLogEntry, Long> {
}
