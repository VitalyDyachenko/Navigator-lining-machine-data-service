package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyBuild;

public interface SurveyBuildRepository extends JpaRepository<SurveyBuild, Long> {
}