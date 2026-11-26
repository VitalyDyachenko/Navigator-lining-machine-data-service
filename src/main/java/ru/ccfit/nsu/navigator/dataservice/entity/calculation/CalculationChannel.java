package ru.ccfit.nsu.navigator.dataservice.entity.calculation;

import jakarta.persistence.*;

@Entity
@Table(name = "calculation_channel")
@IdClass(CalculationChannelId.class)
public class CalculationChannel {

	@Id
	@Column(name = "work_id")
	private Long workId;

	@Id
	@Column(name = "channel_index")
	private Short channelIndex;

	@Column(name = "value_type", nullable = false)
	private short valueType;

	@Column(name = "physical_type_id", nullable = false)
	private short physicalTypeId;

	@Column(name = "scale", nullable = false)
	private float scale;

	@Column(name = "data", columnDefinition = "bytea")
	private byte[] data;

	protected CalculationChannel() {
	}

	public CalculationChannel(
			Long workId,
			short channelIndex,
			short valueType,
			short physicalTypeId,
			float scale,
			byte[] data
	) {
		this.workId = workId;
		this.channelIndex = channelIndex;
		this.valueType = valueType;
		this.physicalTypeId = physicalTypeId;
		this.scale = scale;
		this.data = data.clone();
	}

	public Short getChannelIndex() {
		return channelIndex;
	}

	public short getValueType() {
		return valueType;
	}

	public short getPhysicalTypeId() {
		return physicalTypeId;
	}

	public float getScale() {
		return scale;
	}
}