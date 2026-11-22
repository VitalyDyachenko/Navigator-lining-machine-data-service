package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyMarker;

public interface SurveyMarkerRepository extends JpaRepository<SurveyMarker, Long> {
}