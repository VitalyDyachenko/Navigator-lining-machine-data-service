package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;

import java.time.LocalDateTime;

@Entity
@Table(name = "correction_fragment")
public class CorrectionFragment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "work_id", nullable = false)
	private Long workId;

	@Column(name = "fragment_index", nullable = false)
	private short fragmentIndex;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "machine_params_id", nullable = false)
	private MachineParams machineParams;

	@Column(name = "start_time")
	private LocalDateTime startTime;

	@Column(name = "end_time")
	private LocalDateTime endTime;

	@Column(name = "start_km")
	private Short startKm;

	@Column(name = "start_picket")
	private Short startPicket;

	@Column(name = "start_m")
	private Float startM;

	@Column(name = "end_km")
	private Short endKm;

	@Column(name = "end_picket")
	private Short endPicket;

	@Column(name = "end_m")
	private Float endM;

	@Column(name = "start_pos_step_tenths", nullable = false)
	private int startPosStepTenths;

	@Column(name = "end_pos_step_tenths", nullable = false)
	private int endPosStepTenths;

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

	@Column(name = "setup_mark")
	private Boolean setupMark;

	@Column(name = "navigator_vpi")
	private Boolean navigatorVpi;

	@Column(name = "channel_mask", nullable = false)
	private int channelMask;

	@Column(name = "step_size_mm", nullable = false)
	private int stepSizeMm;

	@Column(name = "step_count", nullable = false)
	private long stepCount;

	@Column(name = "software_type", length = 4)
	private String softwareType;

	@Column(name = "software_major")
	private Short softwareMajor;

	@Column(name = "software_minor")
	private Short softwareMinor;

	@Column(name = "software_build")
	private Short softwareBuild;

	@Column(name = "software_revision")
	private Short softwareRevision;

	@Column(name = "operator_text", length = 97)
	private String operatorText;

	@Column(name = "remark_mask")
	private Integer remarkMask;

	@Column(name = "start_element_type_id")
	private Short startElementTypeId;

	@Column(name = "end_element_type_id")
	private Short endElementTypeId;

	protected CorrectionFragment() {
	}

	public CorrectionFragment(
			Long workId,
			short fragmentIndex,
			MachineParams machineParams,
			LocalDateTime startTime,
			LocalDateTime endTime,
			short startKm,
			short startPicket,
			float startM,
			short endKm,
			short endPicket,
			float endM,
			int startPosStepTenths,
			int endPosStepTenths,
			int channelMask,
			int stepSizeMm,
			long stepCount
	) {
		this.workId = workId;
		this.fragmentIndex = fragmentIndex;
		this.machineParams = machineParams;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startKm = startKm;
		this.startPicket = startPicket;
		this.startM = startM;
		this.endKm = endKm;
		this.endPicket = endPicket;
		this.endM = endM;
		this.startPosStepTenths = startPosStepTenths;
		this.endPosStepTenths = endPosStepTenths;
		this.channelMask = channelMask;
		this.stepSizeMm = stepSizeMm;
		this.stepCount = stepCount;
	}

	// Установка флагов фрагмента выправки
	public void setFlags(
			boolean againstPicketage,
			boolean movingBackward,
			boolean leftPress,
			boolean rightPress,
			boolean shortChord,
			short intertrackSpace,
			boolean markerAtFront,
			boolean dlsEnabled,
			boolean setupMark,
			boolean navigatorVpi
	) {
		this.againstPicketage = againstPicketage;
		this.movingBackward = movingBackward;
		this.leftPress = leftPress;
		this.rightPress = rightPress;
		this.shortChord = shortChord;
		this.intertrackSpace = intertrackSpace;
		this.markerAtFront = markerAtFront;
		this.dlsEnabled = dlsEnabled;
		this.setupMark = setupMark;
		this.navigatorVpi = navigatorVpi;
	}

	// Установка сведений о версии ПО и примечаниях фрагмента
	public void setMetadata(
			String softwareType,
			short softwareMajor,
			short softwareMinor,
			short softwareBuild,
			short softwareRevision,
			String operatorText,
			int remarkMask,
			short startElementTypeId,
			short endElementTypeId
	) {
		this.softwareType = softwareType;
		this.softwareMajor = softwareMajor;
		this.softwareMinor = softwareMinor;
		this.softwareBuild = softwareBuild;
		this.softwareRevision = softwareRevision;
		this.operatorText = operatorText;
		this.remarkMask = remarkMask;
		this.startElementTypeId = startElementTypeId;
		this.endElementTypeId = endElementTypeId;
	}

	public Long getId() {
		return id;
	}

	public MachineParams getMachineParams() {
		return machineParams;
	}

	public long getStepCount() {
		return stepCount;
	}
}