package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.PassRequest;
import ru.edu.online.entities.enums.PassRequestStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPassRequestRepository extends JpaRepository<PassRequest, UUID> {

    List<PassRequest> findAllByAuthorIdAndStatus(String authorId, PassRequestStatus status);

    List<PassRequest> findAllByAuthorUniversityId(String universityId);

    List<PassRequest> findAllByAuthorId(String authorId);

    List<PassRequest> findAllByTargetUniversityIdAndStatus(String targetUniversityId, PassRequestStatus status);

    List<PassRequest> findAllByStatus(PassRequestStatus status);

    Long countAllByNumberGreaterThan(Long number);
}
