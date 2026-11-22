package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyElevation;

public interface SurveyElevationRepository extends JpaRepository<SurveyElevation, Long> {
}