package gisscos.studentcard.repositories;

import gisscos.studentcard.entities.PassFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPassFileRepository extends JpaRepository<PassFile, Long> {

    @Query("select pf from PassFile pf where pf.name = :filename")
    Optional<PassFile> findByName(@Param("filename") String fileName);
}
