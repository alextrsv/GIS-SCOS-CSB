package ru.edu.online.repositories;

import ru.edu.online.entities.PassRequestChangeLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPassRequestChangeLogRepository extends JpaRepository<PassRequestChangeLogEntry, UUID> {
}
