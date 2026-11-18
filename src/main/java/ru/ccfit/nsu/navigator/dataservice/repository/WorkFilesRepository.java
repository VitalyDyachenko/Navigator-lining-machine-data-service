package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ccfit.nsu.navigator.dataservice.entity.WorkFiles;
import ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse;

import java.util.Optional;

public interface WorkFilesRepository extends JpaRepository<WorkFiles, Long> {

    @Query("""
            SELECT new ru.ccfit.nsu.navigator.dataservice.dto.WorkFilesResponse(
                f.id,
                f.surveyFilename,
                f.calculationFilename,
                f.workFilename
            )
            FROM WorkFiles f
            WHERE f.id = :id
            """)
    Optional<WorkFilesResponse> findFilesInfoById(@Param("id") Long id);
}