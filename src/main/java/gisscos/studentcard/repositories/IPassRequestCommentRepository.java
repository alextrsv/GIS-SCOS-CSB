package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassRequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPassRequestCommentRepository extends JpaRepository<PassRequestComment, UUID> {
}
