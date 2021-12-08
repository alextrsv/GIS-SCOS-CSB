package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.enums.PRStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPRRepository extends JpaRepository<PassRequest, UUID> {

    List<PassRequest> findAllByAuthorUniversityId(String universityId);

    List<PassRequest> findAllByAuthorId(String authorId);

    List<PassRequest> findAllByTargetUniversityIdAndStatus(String targetUniversityId, PRStatus status);

    List<PassRequest> findAllByTargetUniversityId(String targetUniversityId);

    List<PassRequest> findAllByStatus(PRStatus status);

    Long countAllByNumberGreaterThan(Long number);
}
