package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.DynamicQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DynamicQRRepository extends JpaRepository<DynamicQR, Long> {

    Optional<DynamicQR> getByUserId(Long userId);
}
