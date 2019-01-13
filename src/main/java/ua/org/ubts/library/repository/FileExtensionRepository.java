package ua.org.ubts.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.org.ubts.library.entity.FileExtensionEntity;

import java.util.Optional;

@Repository
public interface FileExtensionRepository extends JpaRepository<FileExtensionEntity, Integer> {

    Optional<FileExtensionEntity> findByName(String name);

}
