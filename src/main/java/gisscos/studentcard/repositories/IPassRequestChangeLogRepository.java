package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequestChangeLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPassRequestChangeLogRepository extends JpaRepository<PassRequestChangeLogEntry, UUID> {
}
