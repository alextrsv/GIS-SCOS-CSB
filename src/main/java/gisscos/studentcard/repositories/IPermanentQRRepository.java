package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PermanentQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPermanentQRRepository extends JpaRepository<PermanentQR, Long> {

    List<PermanentQR> findAllByUserId(Long userId);

}
