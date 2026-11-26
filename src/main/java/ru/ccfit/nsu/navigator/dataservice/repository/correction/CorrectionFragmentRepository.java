package ru.ccfit.nsu.navigator.dataservice.repository.correction;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.CorrectionFragment;

public interface CorrectionFragmentRepository extends JpaRepository<CorrectionFragment, Long> {
}