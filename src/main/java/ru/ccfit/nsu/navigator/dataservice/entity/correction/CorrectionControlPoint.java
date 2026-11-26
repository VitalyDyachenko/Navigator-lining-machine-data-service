package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;

@Entity
@Table(name = "correction_control_point")
public class CorrectionControlPoint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fragment_id", nullable = false)
	private Long fragmentId;

	@Column(name = "regim_code", nullable = false)
	private short regimCode;

	@Column(name = "temp_ctrl", nullable = false)
	private short tempCtrl;

	@Column(name = "recorded_time", nullable = false)
	private LocalTime recordedTime;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "interface_regulators", nullable = false, columnDefinition = "smallint[]")
	private short[] interfaceRegulators;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "external_regulators", nullable = false, columnDefinition = "smallint[]")
	private short[] externalRegulators;

	@Column(name = "left_rail_temp_c", nullable = false)
	private short leftRailTempC;

	@Column(name = "right_rail_temp_c", nullable = false)
	private short rightRailTempC;

	@Column(name = "air_temp_c", nullable = false)
	private short airTempC;

	protected CorrectionControlPoint() {
	}

	public CorrectionControlPoint(
			Long fragmentId,
			short regimCode,
			short tempCtrl,
			LocalTime recordedTime,
			short[] interfaceRegulators,
			short[] externalRegulators,
			short leftRailTempC,
			short rightRailTempC,
			short airTempC
	) {
		this.fragmentId = fragmentId;
		this.regimCode = regimCode;
		this.tempCtrl = tempCtrl;
		this.recordedTime = recordedTime;
		this.interfaceRegulators = interfaceRegulators.clone();
		this.externalRegulators = externalRegulators.clone();
		this.leftRailTempC = leftRailTempC;
		this.rightRailTempC = rightRailTempC;
		this.airTempC = airTempC;
	}
}