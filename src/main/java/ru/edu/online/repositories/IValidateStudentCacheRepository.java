package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.CacheStudent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IValidateStudentCacheRepository extends JpaRepository<CacheStudent, UUID> {

    Optional<CacheStudent> findByEmailAndScosId(String email, String scosId);

    List<CacheStudent> findAllByValidationDateBefore(LocalDate localDate);
}
