package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.PassRequestUser;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPassRequestUserRepository extends JpaRepository<PassRequestUser, UUID> {

    Boolean existsByPassRequestIdAndScosId(UUID passRequestId, String scosId);

    List<PassRequestUser> findAllByPassRequestId(UUID PassRequestId);

    List<PassRequestUser> getByScosId(String scosId);
}
