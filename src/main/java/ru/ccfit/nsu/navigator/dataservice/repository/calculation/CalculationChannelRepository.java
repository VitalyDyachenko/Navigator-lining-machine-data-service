package ru.ccfit.nsu.navigator.dataservice.repository.calculation;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationChannelId;

public interface CalculationChannelRepository
		extends JpaRepository<CalculationChannel, CalculationChannelId> {
}