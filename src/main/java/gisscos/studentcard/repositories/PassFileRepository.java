package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassFileRepository extends JpaRepository<PassFile, Long> {
}
