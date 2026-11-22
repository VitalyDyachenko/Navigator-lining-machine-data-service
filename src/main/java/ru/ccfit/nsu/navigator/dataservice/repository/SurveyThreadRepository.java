package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyThread;

public interface SurveyThreadRepository extends JpaRepository<SurveyThread, Long> {
}