package com.analytics.domain.repository;

import com.analytics.domain.entity.DataFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DataFile entity
 */
@Repository
public interface DataFileRepository extends JpaRepository<DataFile, Long> {

    /**
     * Find all files by owner
     */
    List<DataFile> findByOwnerId(Long ownerId);

    /**
     * Find all files by owner username
     */
    @Query("SELECT df FROM DataFile df WHERE df.owner.username = :username")
    List<DataFile> findByOwnerUsername(@Param("username") String username);

    /**
     * Find files by file type
     */
    List<DataFile> findByFileType(String fileType);

    /**
     * Find processed files
     */
    List<DataFile> findByProcessed(boolean processed);

    /**
     * Find file by name and owner
     */
    Optional<DataFile> findByFileNameAndOwnerId(String fileName, Long ownerId);

    /**
     * Count files by owner
     */
    long countByOwnerId(Long ownerId);
}
