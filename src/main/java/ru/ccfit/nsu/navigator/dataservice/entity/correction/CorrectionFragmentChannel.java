package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;

@Entity
@Table(name = "correction_fragment_channel")
@IdClass(CorrectionFragmentChannelId.class)
public class CorrectionFragmentChannel {

	@Id
	@Column(name = "fragment_id")
	private Long fragmentId;

	@Id
	@Column(name = "channel_index")
	private Short channelIndex;

	@Column(name = "data", columnDefinition = "bytea")
	private byte[] data;

	protected CorrectionFragmentChannel() {
	}

	public CorrectionFragmentChannel(Long fragmentId, short channelIndex, byte[] data) {
		this.fragmentId = fragmentId;
		this.channelIndex = channelIndex;
		this.data = data.clone();
	}

	public Short getChannelIndex() {
		return channelIndex;
	}
}