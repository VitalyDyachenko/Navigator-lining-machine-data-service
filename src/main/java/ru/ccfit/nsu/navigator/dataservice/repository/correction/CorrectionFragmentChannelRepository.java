package ru.ccfit.nsu.navigator.dataservice.repository.correction;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.CorrectionFragmentChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.CorrectionFragmentChannelId;

public interface CorrectionFragmentChannelRepository
		extends JpaRepository<CorrectionFragmentChannel, CorrectionFragmentChannelId> {
}