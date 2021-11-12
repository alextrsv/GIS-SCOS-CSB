package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.DynamicQRUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDynamicQRUserRepository extends JpaRepository<DynamicQRUser, Long> {

    List<DynamicQRUser> getByOrganizationId(String organizationId);
}
