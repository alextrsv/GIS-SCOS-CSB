package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.edu.online.entities.PassRequestChangeLogEntry;

import java.util.UUID;

public interface IPassRequestChangeLogRepository extends JpaRepository<PassRequestChangeLogEntry, UUID> {
}
