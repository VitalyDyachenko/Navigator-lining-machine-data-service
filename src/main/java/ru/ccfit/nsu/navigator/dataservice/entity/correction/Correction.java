package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "correction")
public class Correction {

	@Id
	@Column(name = "work_id")
	private Long workId;

	@Column(name = "version", nullable = false)
	private short version;

	@Column(name = "correction_time")
	private LocalDateTime correctionTime;

	@Column(name = "channel_mask", nullable = false)
	private int channelMask;

	@Column(name = "against_picketage")
	private Boolean againstPicketage;

	@Column(name = "moving_backward")
	private Boolean movingBackward;

	@Column(name = "left_press")
	private Boolean leftPress;

	@Column(name = "right_press")
	private Boolean rightPress;

	@Column(name = "short_chord")
	private Boolean shortChord;

	@Column(name = "intertrack_space")
	private Short intertrackSpace;

	@Column(name = "marker_at_front")
	private Boolean markerAtFront;

	@Column(name = "dls_enabled")
	private Boolean dlsEnabled;

	protected Correction() {
	}

	public Correction(
			Long workId,
			short version,
			LocalDateTime correctionTime,
			int channelMask
	) {
		this.workId = workId;
		this.version = version;
		this.correctionTime = correctionTime;
		this.channelMask = channelMask;
	}

	// Установка флагов заголовка истории выправки
	public void setFlags(
			boolean againstPicketage,
			boolean movingBackward,
			boolean leftPress,
			boolean rightPress,
			boolean shortChord,
			short intertrackSpace,
			boolean markerAtFront,
			boolean dlsEnabled
	) {
		this.againstPicketage = againstPicketage;
		this.movingBackward = movingBackward;
		this.leftPress = leftPress;
		this.rightPress = rightPress;
		this.shortChord = shortChord;
		this.intertrackSpace = intertrackSpace;
		this.markerAtFront = markerAtFront;
		this.dlsEnabled = dlsEnabled;
	}

	public Long getWorkId() {
		return workId;
	}

	public short getVersion() {
		return version;
	}

	public LocalDateTime getCorrectionTime() {
		return correctionTime;
	}
}