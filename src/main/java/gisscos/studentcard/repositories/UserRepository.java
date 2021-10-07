package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Long, User> {
    Optional<User> getByToken(String userToken);
}
