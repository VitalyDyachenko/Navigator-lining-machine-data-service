package ru.ccfit.nsu.navigator.dataservice.repository.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
}