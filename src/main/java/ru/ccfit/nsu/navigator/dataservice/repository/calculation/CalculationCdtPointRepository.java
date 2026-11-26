package ru.ccfit.nsu.navigator.dataservice.repository.calculation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationCdtPoint;

public interface CalculationCdtPointRepository extends JpaRepository<CalculationCdtPoint, Long> {
}