package ru.ccfit.nsu.navigator.dataservice.repository.survey.vpi;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi.SurveyDefect;

public interface SurveyDefectRepository extends JpaRepository<SurveyDefect, Long> {
}