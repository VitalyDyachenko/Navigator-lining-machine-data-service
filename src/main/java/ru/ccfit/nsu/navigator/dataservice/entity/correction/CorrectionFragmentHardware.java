package ru.ccfit.nsu.navigator.dataservice.entity.correction;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "correction_fragment_hardware")
public class CorrectionFragmentHardware {

	@Id
	@Column(name = "fragment_id")
	private Long fragmentId;

	@Column(name = "mnk_major", nullable = false)
	private short mnkMajor;

	@Column(name = "mnk_minor", nullable = false)
	private short mnkMinor;

	@Column(name = "mnk_description", length = 12)
	private String mnkDescription;

	@Column(name = "system_major", nullable = false)
	private short systemMajor;

	@Column(name = "system_minor", nullable = false)
	private short systemMinor;

	@Column(name = "system_description", length = 12)
	private String systemDescription;

	@Column(name = "rfid_major", nullable = false)
	private short rfidMajor;

	@Column(name = "rfid_minor", nullable = false)
	private short rfidMinor;

	@Column(name = "rfid_description", length = 12)
	private String rfidDescription;

	@Column(name = "rfb_major", nullable = false)
	private short rfbMajor;

	@Column(name = "rfb_minor", nullable = false)
	private short rfbMinor;

	@Column(name = "rfb_description", length = 12)
	private String rfbDescription;

	@Column(name = "step_ms_error_tenth", nullable = false)
	private short stepMsErrorTenth;

	@Column(name = "step_calibration_date")
	private LocalDate stepCalibrationDate;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "aout_ms_error_tenth", nullable = false, columnDefinition = "smallint[]")
	private short[] aoutMsErrorTenth;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "aout_calibration_dates", nullable = false, columnDefinition = "date[]")
	private LocalDate[] aoutCalibrationDates;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "ain_ms_error_tenth", nullable = false, columnDefinition = "smallint[]")
	private short[] ainMsErrorTenth;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "ain_calibration_dates", nullable = false, columnDefinition = "date[]")
	private LocalDate[] ainCalibrationDates;

	protected CorrectionFragmentHardware() {
	}

	public CorrectionFragmentHardware(
			Long fragmentId,
			short mnkMajor,
			short mnkMinor,
			String mnkDescription,
			short systemMajor,
			short systemMinor,
			String systemDescription,
			short rfidMajor,
			short rfidMinor,
			String rfidDescription,
			short rfbMajor,
			short rfbMinor,
			String rfbDescription,
			short stepMsErrorTenth,
			LocalDate stepCalibrationDate,
			short[] aoutMsErrorTenth,
			LocalDate[] aoutCalibrationDates,
			short[] ainMsErrorTenth,
			LocalDate[] ainCalibrationDates
	) {
		this.fragmentId = fragmentId;
		this.mnkMajor = mnkMajor;
		this.mnkMinor = mnkMinor;
		this.mnkDescription = mnkDescription;
		this.systemMajor = systemMajor;
		this.systemMinor = systemMinor;
		this.systemDescription = systemDescription;
		this.rfidMajor = rfidMajor;
		this.rfidMinor = rfidMinor;
		this.rfidDescription = rfidDescription;
		this.rfbMajor = rfbMajor;
		this.rfbMinor = rfbMinor;
		this.rfbDescription = rfbDescription;
		this.stepMsErrorTenth = stepMsErrorTenth;
		this.stepCalibrationDate = stepCalibrationDate;
		this.aoutMsErrorTenth = aoutMsErrorTenth.clone();
		this.aoutCalibrationDates = aoutCalibrationDates.clone();
		this.ainMsErrorTenth = ainMsErrorTenth.clone();
		this.ainCalibrationDates = ainCalibrationDates.clone();
	}

	public Long getFragmentId() {
		return fragmentId;
	}

	public LocalDate[] getAoutCalibrationDates() {
		return aoutCalibrationDates.clone();
	}

	public LocalDate[] getAinCalibrationDates() {
		return ainCalibrationDates.clone();
	}
}