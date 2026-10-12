package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.OriginalFiles;

public interface OriginalFilesRepository extends JpaRepository<OriginalFiles, Long> {
}