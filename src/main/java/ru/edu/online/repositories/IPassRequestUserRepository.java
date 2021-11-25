package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface IPassRequestUserRepository extends JpaRepository<User, UUID> {

    Boolean existsByPassRequestIdAndUserId(UUID passRequestId, String userId);

    List<User> findAllByPassRequestId(UUID PassRequestId);

    List<User> getByUserId(String userId);
}
