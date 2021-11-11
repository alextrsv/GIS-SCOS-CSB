package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.CacheStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IValidateStudentCacheRepository extends JpaRepository<CacheStudent, UUID> {

    Optional<CacheStudent> findByScosId(UUID uuid);

    boolean deleteByValidationDateBefore(LocalDate date);
}
