package ru.ccfit.nsu.navigator.dataservice.repository.correction;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.CorrectionPruCycle;

public interface CorrectionPruCycleRepository extends JpaRepository<CorrectionPruCycle, Long> {
}