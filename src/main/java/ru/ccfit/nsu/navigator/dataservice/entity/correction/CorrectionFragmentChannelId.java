package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import java.io.Serializable;
import java.util.Objects;

public class CorrectionFragmentChannelId implements Serializable {

	private Long fragmentId;
	private Short channelIndex;

	public CorrectionFragmentChannelId() {
	}

	public CorrectionFragmentChannelId(Long fragmentId, Short channelIndex) {
		this.fragmentId = fragmentId;
		this.channelIndex = channelIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CorrectionFragmentChannelId other)) return false;

		return Objects.equals(fragmentId, other.fragmentId)
				&& Objects.equals(channelIndex, other.channelIndex);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fragmentId, channelIndex);
	}
}