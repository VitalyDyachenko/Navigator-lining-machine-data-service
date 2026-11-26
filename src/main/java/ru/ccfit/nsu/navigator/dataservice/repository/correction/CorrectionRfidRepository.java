package ru.ccfit.nsu.navigator.dataservice.repository.correction;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.CorrectionRfid;

public interface CorrectionRfidRepository extends JpaRepository<CorrectionRfid, Long> {
}