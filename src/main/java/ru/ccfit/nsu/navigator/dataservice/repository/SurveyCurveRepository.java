package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyCurve;

public interface SurveyCurveRepository extends JpaRepository<SurveyCurve, Long> {
}