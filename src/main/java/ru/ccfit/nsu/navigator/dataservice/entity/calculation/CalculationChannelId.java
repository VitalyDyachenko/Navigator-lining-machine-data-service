package ru.ccfit.nsu.navigator.dataservice.entity.calculation;

import java.io.Serializable;
import java.util.Objects;

public class CalculationChannelId implements Serializable {

	private Long workId;
	private Short channelIndex;

	public CalculationChannelId() {
	}

	public CalculationChannelId(Long workId, Short channelIndex) {
		this.workId = workId;
		this.channelIndex = channelIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CalculationChannelId other)) return false;

		return Objects.equals(workId, other.workId)
				&& Objects.equals(channelIndex, other.channelIndex);
	}

	@Override
	public int hashCode() {
		return Objects.hash(workId, channelIndex);
	}
}