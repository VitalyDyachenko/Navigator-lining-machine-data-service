package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.WorkFiles;

public interface WorkFilesRepository extends JpaRepository<WorkFiles, Long> {
}