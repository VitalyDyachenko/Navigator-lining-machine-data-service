package ru.ccfit.nsu.navigator.dataservice.repository.survey.vpi;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi.SurveyThread;

public interface SurveyThreadRepository extends JpaRepository<SurveyThread, Long> {
}