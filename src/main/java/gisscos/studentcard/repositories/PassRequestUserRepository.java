package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassRequestUserRepository extends JpaRepository<PassRequestUser, Long> {
    Boolean existsByPassRequestIdAndUserId(Long userId, Long passRequestId);

    List<PassRequestUser> findAllByPassRequestId(Long PassRequestId);
}
