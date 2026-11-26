package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "correction_pru_cycle")
public class CorrectionPruCycle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fragment_id", nullable = false)
	private Long fragmentId;

	@Column(name = "pos_step_tenths", nullable = false)
	private int posStepTenths;

	@Column(name = "recorded_time", nullable = false)
	private LocalTime recordedTime;

	@Column(name = "cycle_duration_tenth_sec", nullable = false)
	private short cycleDurationTenthSec;

	@Column(name = "cycle_number", nullable = false)
	private short cycleNumber;

	@Column(name = "is_right_press", nullable = false)
	private boolean rightPress;

	@Column(name = "alignment_enabled", nullable = false)
	private boolean alignmentEnabled;

	@Column(name = "left_lift_enabled", nullable = false)
	private boolean leftLiftEnabled;

	@Column(name = "right_lift_enabled", nullable = false)
	private boolean rightLiftEnabled;

	@Column(name = "delta_alignment", nullable = false)
	private short deltaAlignment;

	@Column(name = "delta_left_lift", nullable = false)
	private short deltaLeftLift;

	@Column(name = "delta_right_lift", nullable = false)
	private short deltaRightLift;

	@Column(name = "alert_regim", nullable = false)
	private boolean alertRegim;

	@Column(name = "alert_skip_track", nullable = false)
	private boolean alertSkipTrack;

	@Column(name = "alert_rollback", nullable = false)
	private boolean alertRollback;

	protected CorrectionPruCycle() {
	}

	public CorrectionPruCycle(
			Long fragmentId,
			int posStepTenths,
			LocalTime recordedTime,
			short cycleDurationTenthSec,
			short cycleNumber,
			boolean rightPress,
			boolean alignmentEnabled,
			boolean leftLiftEnabled,
			boolean rightLiftEnabled,
			short deltaAlignment,
			short deltaLeftLift,
			short deltaRightLift,
			boolean alertRegim,
			boolean alertSkipTrack,
			boolean alertRollback
	) {
		this.fragmentId = fragmentId;
		this.posStepTenths = posStepTenths;
		this.recordedTime = recordedTime;
		this.cycleDurationTenthSec = cycleDurationTenthSec;
		this.cycleNumber = cycleNumber;
		this.rightPress = rightPress;
		this.alignmentEnabled = alignmentEnabled;
		this.leftLiftEnabled = leftLiftEnabled;
		this.rightLiftEnabled = rightLiftEnabled;
		this.deltaAlignment = deltaAlignment;
		this.deltaLeftLift = deltaLeftLift;
		this.deltaRightLift = deltaRightLift;
		this.alertRegim = alertRegim;
		this.alertSkipTrack = alertSkipTrack;
		this.alertRollback = alertRollback;
	}
}