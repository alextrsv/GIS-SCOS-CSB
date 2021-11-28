package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.DynamicQRUser;

import java.util.List;
import java.util.UUID;

@Repository
public interface IDynamicQRUserRepository extends JpaRepository<DynamicQRUser, UUID> {

    List<DynamicQRUser> getByOrganizationId(String organizationId);

    Boolean existsByUserId(String userId);

    Boolean existsByUserIdAndOrganizationId(String userId, String organizationId);
}
