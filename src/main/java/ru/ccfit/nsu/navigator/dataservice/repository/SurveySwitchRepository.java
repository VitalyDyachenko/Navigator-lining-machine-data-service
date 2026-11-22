package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveySwitch;

public interface SurveySwitchRepository extends JpaRepository<SurveySwitch, Long> {
}