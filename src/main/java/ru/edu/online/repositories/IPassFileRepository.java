package ru.edu.online.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.edu.online.entities.PassFile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPassFileRepository extends JpaRepository<PassFile, UUID> {

    @Query("select pf from PassFile pf where pf.name = :filename")
    Optional<PassFile> findByName(@Param("filename") String fileName);
}