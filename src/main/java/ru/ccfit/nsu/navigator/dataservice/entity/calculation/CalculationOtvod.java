package ru.ccfit.nsu.navigator.dataservice.entity.calculation;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_otvod")
public class CalculationOtvod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "work_id", nullable = false)
	private Long workId;

	@Column(name = "otvod_index", nullable = false)
	private short otvodIndex;

	@Column(name = "is_left", nullable = false)
	private boolean left;

	@Column(name = "is_right", nullable = false)
	private boolean right;

	@Column(name = "is_non_linear", nullable = false)
	private boolean nonLinear;

	@Column(name = "is_proportional", nullable = false)
	private boolean proportional;

	@Column(name = "is_corrected", nullable = false)
	private boolean corrected;

	@Column(name = "is_plane", nullable = false)
	private boolean plane;

	@Column(name = "is_level", nullable = false)
	private boolean level;

	@Column(name = "is_profile", nullable = false)
	private boolean profile;

	@Column(name = "mode_id", nullable = false)
	private short modeId;

	@Column(name = "source_id", nullable = false)
	private short sourceId;

	@Column(name = "is_auto_by_clearance", nullable = false)
	private boolean autoByClearance;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "start_points_m", nullable = false, columnDefinition = "double precision[]")
	private double[] startPointsM;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "end_points_m", nullable = false, columnDefinition = "double precision[]")
	private double[] endPointsM;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "start_coefficients", nullable = false, columnDefinition = "double precision[]")
	private double[] startCoefficients;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "end_coefficients", nullable = false, columnDefinition = "double precision[]")
	private double[] endCoefficients;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "start_initial_y_tenth_mm", nullable = false, columnDefinition = "smallint[]")
	private short[] startInitialYTenthMm;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "end_initial_y_tenth_mm", nullable = false, columnDefinition = "smallint[]")
	private short[] endInitialYTenthMm;

	@Column(name = "delta_plane_left_tenth_mm", nullable = false)
	private short deltaPlaneLeftTenthMm;

	@Column(name = "delta_plane_right_tenth_mm", nullable = false)
	private short deltaPlaneRightTenthMm;

	@Column(name = "delta_profile_left_tenth_mm", nullable = false)
	private short deltaProfileLeftTenthMm;

	@Column(name = "delta_profile_right_tenth_mm", nullable = false)
	private short deltaProfileRightTenthMm;

	@Column(name = "creation_time")
	private LocalDateTime creationTime;

	protected CalculationOtvod() {
	}

	public CalculationOtvod(
			Long workId,
			short otvodIndex,
			boolean left,
			boolean right,
			boolean nonLinear,
			boolean proportional,
			boolean corrected,
			boolean plane,
			boolean level,
			boolean profile,
			short modeId,
			short sourceId,
			boolean autoByClearance,
			double[] startPointsM,
			double[] endPointsM,
			double[] startCoefficients,
			double[] endCoefficients,
			short[] startInitialYTenthMm,
			short[] endInitialYTenthMm,
			short deltaPlaneLeftTenthMm,
			short deltaPlaneRightTenthMm,
			short deltaProfileLeftTenthMm,
			short deltaProfileRightTenthMm,
			LocalDateTime creationTime
	) {
		this.workId = workId;
		this.otvodIndex = otvodIndex;
		this.left = left;
		this.right = right;
		this.nonLinear = nonLinear;
		this.proportional = proportional;
		this.corrected = corrected;
		this.plane = plane;
		this.level = level;
		this.profile = profile;
		this.modeId = modeId;
		this.sourceId = sourceId;
		this.autoByClearance = autoByClearance;
		this.startPointsM = startPointsM.clone();
		this.endPointsM = endPointsM.clone();
		this.startCoefficients = startCoefficients.clone();
		this.endCoefficients = endCoefficients.clone();
		this.startInitialYTenthMm = startInitialYTenthMm.clone();
		this.endInitialYTenthMm = endInitialYTenthMm.clone();
		this.deltaPlaneLeftTenthMm = deltaPlaneLeftTenthMm;
		this.deltaPlaneRightTenthMm = deltaPlaneRightTenthMm;
		this.deltaProfileLeftTenthMm = deltaProfileLeftTenthMm;
		this.deltaProfileRightTenthMm = deltaProfileRightTenthMm;
		this.creationTime = creationTime;
	}

	public boolean isPlane() {
		return plane;
	}

	public boolean isLevel() {
		return level;
	}

	public boolean isProfile() {
		return profile;
	}
}