package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyRfid;

public interface SurveyRfidRepository extends JpaRepository<SurveyRfid, Long> {
}