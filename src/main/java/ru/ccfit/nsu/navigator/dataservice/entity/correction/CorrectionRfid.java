package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;

@Entity
@Table(name = "correction_rfid")
public class CorrectionRfid {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fragment_id", nullable = false)
	private Long fragmentId;

	@Column(name = "timestamp_ms", nullable = false)
	private long timestampMs;

	@Column(name = "sync_pulse_count", nullable = false)
	private long syncPulseCount;

	@Column(name = "tag_identifier", nullable = false)
	private long tagIdentifier;

	@Column(name = "data_block", nullable = false)
	private long dataBlock;

	@Column(name = "odometer_delta_len_m")
	private float odometerDeltaLenM;

	@Column(name = "odometer_scale_adj")
	private float odometerScaleAdj;

	@Column(name = "block_mask", nullable = false)
	private short blockMask;

	@Column(name = "point_count", nullable = false)
	private short pointCount;

	@Column(name = "has_error", nullable = false)
	private boolean hasError;

	@Column(name = "control_distance_m", nullable = false)
	private double controlDistanceM;

	protected CorrectionRfid() {
	}

	public CorrectionRfid(
			Long fragmentId,
			long timestampMs,
			long syncPulseCount,
			long tagIdentifier,
			long dataBlock,
			float odometerDeltaLenM,
			float odometerScaleAdj,
			short blockMask,
			short pointCount,
			boolean hasError,
			double controlDistanceM
	) {
		this.fragmentId = fragmentId;
		this.timestampMs = timestampMs;
		this.syncPulseCount = syncPulseCount;
		this.tagIdentifier = tagIdentifier;
		this.dataBlock = dataBlock;
		this.odometerDeltaLenM = odometerDeltaLenM;
		this.odometerScaleAdj = odometerScaleAdj;
		this.blockMask = blockMask;
		this.pointCount = pointCount;
		this.hasError = hasError;
		this.controlDistanceM = controlDistanceM;
	}
}