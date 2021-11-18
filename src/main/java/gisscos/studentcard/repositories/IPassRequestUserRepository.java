package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPassRequestUserRepository extends JpaRepository<PassRequestUser, Long> {
    Boolean existsByPassRequestIdAndUserId( Long passRequestId, String userId);

    List<PassRequestUser> findAllByPassRequestId(Long PassRequestId);

    List<PassRequestUser> getByUserId(String userId);
}
