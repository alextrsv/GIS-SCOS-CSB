package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.DynamicQR;

import java.util.List;
import java.util.UUID;

@Repository
public interface IDynamicQRRepository extends JpaRepository<DynamicQR, UUID> {

    List<DynamicQR> getByUserId(String userId);

    List<DynamicQR> getByUserIdAndUniversityId(String userId, String universityId);

    List<DynamicQR> getByUniversityId(String organizationId);
}
