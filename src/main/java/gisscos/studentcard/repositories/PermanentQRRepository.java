package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PermanentQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermanentQRRepository extends JpaRepository<PermanentQR, Long> {

}
