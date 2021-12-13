package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.PassRequestFile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPRFileRepository extends JpaRepository<PassRequestFile, UUID> {

    @Query("select pf from PassRequestFile pf where pf.name = :filename")
    Optional<PassRequestFile> findByName(@Param("filename") String fileName);
}
