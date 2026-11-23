package ru.ccfit.nsu.navigator.dataservice.repository.survey.vpi;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi.SurveyBuild;

public interface SurveyBuildRepository extends JpaRepository<SurveyBuild, Long> {
}