package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.DynamicQRUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IDynamicQRUserRepository extends JpaRepository<DynamicQRUser, Long> {

    List<DynamicQRUser> getByOrganizationId(String organizationId);

    Boolean existsByUserId(String userId);
}
