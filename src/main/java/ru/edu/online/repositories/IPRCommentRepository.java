package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.PassRequestComment;

import java.util.UUID;

@Repository
public interface IPRCommentRepository extends JpaRepository<PassRequestComment, UUID> {
}
