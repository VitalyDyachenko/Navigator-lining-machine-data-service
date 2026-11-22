package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyDefect;

public interface SurveyDefectRepository extends JpaRepository<SurveyDefect, Long> {
}