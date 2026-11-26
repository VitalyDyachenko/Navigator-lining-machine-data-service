package ru.ccfit.nsu.navigator.dataservice.entity.calculation;

import jakarta.persistence.*;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;

import java.time.LocalDateTime;

@Entity
@Table(name = "calculation")
public class Calculation {

	@Id
	@Column(name = "work_id")
	private Long workId;

	@Column(name = "version", nullable = false)
	private short version;

	@Column(name = "calculation_time")
	private LocalDateTime calculationTime;

	@Column(name = "computer_name", length = 24)
	private String computerName;

	@Column(name = "channel_mask", nullable = false)
	private int channelMask;

	@Column(name = "start_km")
	private Integer startKm;

	@Column(name = "start_m")
	private Short startM;

	@Column(name = "start_mm")
	private Short startMm;

	@Column(name = "end_km")
	private Integer endKm;

	@Column(name = "end_m")
	private Short endM;

	@Column(name = "end_mm")
	private Short endMm;

	@Column(name = "otvod_count", nullable = false)
	private int otvodCount;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "machine_params_id", nullable = false)
	private MachineParams machineParams;

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

	@Column(name = "dls_enabled")
	private Boolean dlsEnabled;

	@Column(name = "warning")
	private Boolean warning;

	// Поля tsSETUP_PROJECT
	@Column(name = "setup_passenger_speed")
	private Integer setupPassengerSpeed;

	@Column(name = "setup_freight_speed")
	private Integer setupFreightSpeed;

	@Column(name = "setup_max_shift_left_mm")
	private Integer setupMaxShiftLeftMm;

	@Column(name = "setup_max_shift_right_mm")
	private Integer setupMaxShiftRightMm;

	@Column(name = "setup_max_lift_mm")
	private Integer setupMaxLiftMm;

	@Column(name = "setup_min_lift_mm")
	private Integer setupMinLiftMm;

	// Поля tsEXTRA_EXIT
	@Column(name = "extra_exit_machine_pos")
	private Integer extraExitMachinePos;

	@Column(name = "extra_exit_plane_front_pos")
	private Integer extraExitPlaneFrontPos;

	@Column(name = "extra_exit_time_seconds")
	private Long extraExitTimeSeconds;

	@Column(name = "extra_exit_status")
	private Short extraExitStatus;

	@Column(name = "extra_exit_against_picketage")
	private Boolean extraExitAgainstPicketage;

	@Column(name = "extra_exit_moving_backward")
	private Boolean extraExitMovingBackward;

	@Column(name = "extra_exit_left_press")
	private Boolean extraExitLeftPress;

	@Column(name = "extra_exit_right_press")
	private Boolean extraExitRightPress;

	@Column(name = "extra_exit_short_chord")
	private Boolean extraExitShortChord;

	@Column(name = "extra_exit_intertrack_space")
	private Short extraExitIntertrackSpace;

	@Column(name = "extra_exit_marker_at_front")
	private Boolean extraExitMarkerAtFront;

	@Column(name = "extra_exit_dls_enabled")
	private Boolean extraExitDlsEnabled;

	// Поля tsCDT_H
	@Column(name = "cdt_base_rail")
	private Short cdtBaseRail;

	@Column(name = "cdt_points_mask")
	private Short cdtPointsMask;

	@Column(name = "cdt_base_offset_mm")
	private Short cdtBaseOffsetMm;

	@Column(name = "cdt_objects_count")
	private Integer cdtObjectsCount;

	protected Calculation() {
	}

	public Calculation(
			Long workId,
			MachineParams machineParams,
			short version,
			LocalDateTime calculationTime,
			String computerName,
			int channelMask,
			int otvodCount
	) {
		this.workId = workId;
		this.machineParams = machineParams;
		this.version = version;
		this.calculationTime = calculationTime;
		this.computerName = computerName;
		this.channelMask = channelMask;
		this.otvodCount = otvodCount;
	}

	// Установка флагов заголовка расчёта
	public void setFlags(
			boolean againstPicketage,
			boolean movingBackward,
			boolean leftPress,
			boolean rightPress,
			boolean shortChord,
			boolean dlsEnabled,
			boolean warning
	) {
		this.againstPicketage = againstPicketage;
		this.movingBackward = movingBackward;
		this.leftPress = leftPress;
		this.rightPress = rightPress;
		this.shortChord = shortChord;
		this.dlsEnabled = dlsEnabled;
		this.warning = warning;
	}

	// Установка параметров настройки проекта
	public void setSetup(
			int passengerSpeed,
			int freightSpeed,
			int maxShiftLeftMm,
			int maxShiftRightMm,
			int maxLiftMm,
			int minLiftMm
	) {
		this.setupPassengerSpeed = passengerSpeed;
		this.setupFreightSpeed = freightSpeed;
		this.setupMaxShiftLeftMm = maxShiftLeftMm;
		this.setupMaxShiftRightMm = maxShiftRightMm;
		this.setupMaxLiftMm = maxLiftMm;
		this.setupMinLiftMm = minLiftMm;
	}

	// Установка сведений экстренного выхода
	public void setExtraExit(
			int machinePos,
			int planeFrontPos,
			long timeSeconds,
			short status,
			boolean againstPicketage,
			boolean movingBackward,
			boolean leftPress,
			boolean rightPress,
			boolean shortChord,
			short intertrackSpace,
			boolean markerAtFront,
			boolean dlsEnabled
	) {
		this.extraExitMachinePos = machinePos;
		this.extraExitPlaneFrontPos = planeFrontPos;
		this.extraExitTimeSeconds = timeSeconds;
		this.extraExitStatus = status;
		this.extraExitAgainstPicketage = againstPicketage;
		this.extraExitMovingBackward = movingBackward;
		this.extraExitLeftPress = leftPress;
		this.extraExitRightPress = rightPress;
		this.extraExitShortChord = shortChord;
		this.extraExitIntertrackSpace = intertrackSpace;
		this.extraExitMarkerAtFront = markerAtFront;
		this.extraExitDlsEnabled = dlsEnabled;
	}

	// Установка метаданных заголовка CDT
	public void setCdtHeader(
			short baseRail,
			short pointsMask,
			short baseOffsetMm,
			int objectsCount
	) {
		this.cdtBaseRail = baseRail;
		this.cdtPointsMask = pointsMask;
		this.cdtBaseOffsetMm = baseOffsetMm;
		this.cdtObjectsCount = objectsCount;
	}

	public Long getWorkId() {
		return workId;
	}

	public MachineParams getMachineParams() {
		return machineParams;
	}

	public LocalDateTime getCalculationTime() {
		return calculationTime;
	}

	// Установка границ пикетажа проекта
	public void setTrackRange(
			int startKm, short startM, short startMm,
			int endKm, short endM, short endMm
	) {
		this.startKm = startKm;
		this.startM = startM;
		this.startMm = startMm;
		this.endKm = endKm;
		this.endM = endM;
		this.endMm = endMm;
	}

	public Integer getStartKm() {
		return startKm;
	}

	public Short getStartM() {
		return startM;
	}

	public Short getStartMm() {
		return startMm;
	}

	public Integer getEndKm() {
		return endKm;
	}

	public Short getEndM() {
		return endM;
	}

	public Short getEndMm() {
		return endMm;
	}
}