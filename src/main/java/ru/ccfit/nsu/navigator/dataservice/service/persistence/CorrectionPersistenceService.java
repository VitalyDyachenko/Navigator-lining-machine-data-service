package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.*;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.PicketPosition;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.marker.MarkerData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.repository.correction.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CorrectionPersistenceService {

	private static final int MAX_CHANNEL_MASK = 65535;
	private static final int MAX_REMARK_MASK = 65535;
	private static final int MAX_INTERTRACK_SPACE = 3;
	private static final int MAX_MARKER_TYPE_ID = 17;
	private static final int MAX_CYCLE_DURATION_TENTH_SEC = 127;
	private static final int MAX_CYCLE_NUMBER = 3;
	private static final int MAX_RFID_BLOCK_MASK = 15;
	private static final int MAX_RFID_POINT_COUNT = 2047;
	private static final int MAX_CTRL_REGIM_CODE = 8191;
	private static final int MAX_CTRL_TEMP_CTRL = 3;
	private static final int CALIBRATION_CHANNEL_COUNT = 8;

	private final CorrectionRepository correctionRepository;
	private final CorrectionFragmentRepository fragmentRepository;
	private final CorrectionFragmentHardwareRepository hardwareRepository;
	private final CorrectionFragmentChannelRepository channelRepository;
	private final CorrectionPruCycleRepository pruCycleRepository;
	private final CorrectionMarkerRepository markerRepository;
	private final CorrectionRfidRepository rfidRepository;
	private final CorrectionControlPointRepository controlPointRepository;
	private final MachinePersistenceService machinePersistenceService;

	public CorrectionPersistenceService(
			CorrectionRepository correctionRepository,
			CorrectionFragmentRepository fragmentRepository,
			CorrectionFragmentHardwareRepository hardwareRepository,
			CorrectionFragmentChannelRepository channelRepository,
			CorrectionPruCycleRepository pruCycleRepository,
			CorrectionMarkerRepository markerRepository,
			CorrectionRfidRepository rfidRepository,
			CorrectionControlPointRepository controlPointRepository,
			MachinePersistenceService machinePersistenceService
	) {
		this.correctionRepository = correctionRepository;
		this.fragmentRepository = fragmentRepository;
		this.hardwareRepository = hardwareRepository;
		this.channelRepository = channelRepository;
		this.pruCycleRepository = pruCycleRepository;
		this.markerRepository = markerRepository;
		this.rfidRepository = rfidRepository;
		this.controlPointRepository = controlPointRepository;
		this.machinePersistenceService = machinePersistenceService;
	}

	// Сохранение истории выправки с предварительной кросс-валидацией относительно съёмки
	public void save(Long workId, SrvSurvey survey, CrmCorrection correction) {
		validateConsistency(survey, correction);

		CrmHeader header = correction.header();
		saveCorrection(workId, header);

		List<CrmFragment> fragments = correction.fragments();
		for (int i = 0; i < fragments.size(); i++) {
			saveFragment(workId, i, fragments.get(i));
		}
	}

	// Сверка общих структур work между файлами съёмки и выправки
	public void validateConsistency(SrvSurvey survey, CrmCorrection correction) {
		SrvHeader srv = survey.header();
		CrmHeader crm = correction.header();

		if (crm.stepSizeMm() != srv.stepSizeMm()) {
			throw new CrmParsingException(
					"Step size mismatch with SRV: CRM=" + crm.stepSizeMm()
							+ ", SRV=" + srv.stepSizeMm()
			);
		}

		if (crm.stepCount() != srv.stepCount()) {
			throw new CrmParsingException(
					"Step count mismatch with SRV: CRM=" + crm.stepCount()
							+ ", SRV=" + srv.stepCount()
			);
		}

		validateTrackConsistency(srv.track(), crm.track());
		validateMachineIdentity(srv.machine(), crm.machine(), "header");

		List<CrmFragment> fragments = correction.fragments();
		for (int i = 0; i < fragments.size(); i++) {
			validateMachineIdentity(
					srv.machine(),
					fragments.get(i).header().machine(),
					"fragment " + i
			);
		}
	}

	private void validateTrackConsistency(TrackData srvTrack, TrackData crmTrack) {
		if (!srvTrack.hasSameMetadata(crmTrack)) {
			throw new CrmParsingException("Track metadata mismatch with SRV");
		}
	}

	private void validateMachineIdentity(MachineData srvMachine, MachineData crmMachine, String source) {
		if (srvMachine.machineType() != crmMachine.machineType()
				|| srvMachine.number() != crmMachine.number()
				|| !Objects.equals(srvMachine.name(), crmMachine.name())) {
			throw new CrmParsingException("Machine identity mismatch with SRV in " + source);
		}
	}

	private void saveCorrection(Long workId, CrmHeader header) {
		validateChannelMask(header.channelMask(), "header");
		SurveyFlags flags = header.flags();
		validatePress(flags.press(), "header");
		validateIntertrackSpace(flags.intertrackSpace(), "header");

		TrackData track = header.track();
		Correction entity = new Correction(
				workId,
				(short) header.version(),
				header.correctionTime(),
				header.channelMask()
		);

		entity.setTrackRange(
				track.begin().km(), track.begin().meters(), track.begin().millimeters(),
				track.end().km(), track.end().meters(), track.end().millimeters()
		);

		entity.setFlags(
				flags.againstPicketage(),
				flags.movingBackward(),
				flags.press() == 0 || flags.press() == 3,
				flags.press() == 1 || flags.press() == 3,
				flags.shortChord(),
				(short) flags.intertrackSpace(),
				flags.markerAtFront(),
				flags.dlsEnabled()
		);

		correctionRepository.saveAndFlush(entity);
	}

	private void saveFragment(Long workId, int fragmentIndex, CrmFragment source) {
		CrmFragmentHeader header = source.header();
		CrmFragmentData data = source.data();

		validateFragmentHeader(header);

		Machine machine = machinePersistenceService.getOrCreateMachine(header.machine());
		MachineParams params = machinePersistenceService.getOrCreateMachineParams(machine, header.machine());

		PicketPosition startPicket = header.startPicket();
		PicketPosition endPicket = header.endPicket();

		CorrectionFragment fragment = new CorrectionFragment(
				workId,
				(short) fragmentIndex,
				params,
				header.startTime(),
				header.endTime(),
				(short) startPicket.kilometer(),
				(short) startPicket.picket(),
				startPicket.meter(),
				(short) endPicket.kilometer(),
				(short) endPicket.picket(),
				endPicket.meter(),
				header.startPosStepTenths(),
				header.endPosStepTenths(),
				header.channelMask(),
				header.stepSizeMm(),
				header.stepCount()
		);

		CrmFragmentFlags flags = header.flags();
		fragment.setFlags(
				flags.againstPicketage(),
				flags.movingBackward(),
				flags.press() == 0 || flags.press() == 3,
				flags.press() == 1 || flags.press() == 3,
				flags.shortChord(),
				(short) flags.intertrackSpace(),
				flags.markerAtFront(),
				flags.dlsEnabled(),
				flags.setupMark(),
				flags.navigatorVpi()
		);

		CrmSoftwareVersion sw = header.software();
		fragment.setMetadata(
				sw.type(),
				(short) sw.major(),
				(short) sw.minor(),
				(short) sw.build(),
				(short) sw.revision(),
				header.operatorText(),
				header.remarkMask(),
				(short) header.startElementType(),
				(short) header.endElementType()
		);

		CorrectionFragment saved = fragmentRepository.save(fragment);
		Long fragmentId = saved.getId();

		if (header.hasExtendedMetadata()) {
			saveHardware(fragmentId, header);
		}
		saveChannels(fragmentId, data.channelData());
		saveCycles(fragmentId, data.cycles());
		saveMarkers(fragmentId, data.markers());
		saveRfids(fragmentId, data.rfids());
		saveControlPoints(fragmentId, data.controlPoints());
	}

	private void validateFragmentHeader(CrmFragmentHeader header) {
		validatePicket(header.startPicket(), "start");
		validatePicket(header.endPicket(), "end");

		if (header.startPosStepTenths() > header.endPosStepTenths()) {
			throw new CrmParsingException("Invalid IdPOS order in fragment");
		}

		CrmFragmentFlags flags = header.flags();
		validatePress(flags.press(), "fragment");
		validateIntertrackSpace(flags.intertrackSpace(), "fragment");
		validateChannelMask(header.channelMask(), "fragment");

		if (header.remarkMask() < 0 || header.remarkMask() > MAX_REMARK_MASK) {
			throw new CrmParsingException("Unsupported remark mask: " + header.remarkMask());
		}

		validateTrackElementType(header.startElementType(), "start");
		validateTrackElementType(header.endElementType(), "end");
	}

	private void validatePicket(PicketPosition position, String boundary) {
		if (position.kilometer() < Short.MIN_VALUE || position.kilometer() > Short.MAX_VALUE) {
			throw new CrmParsingException("Unsupported " + boundary + " kilometer: " + position.kilometer());
		}
		if (position.picket() < 0 || position.picket() > 9) {
			throw new CrmParsingException("Unsupported " + boundary + " picket: " + position.picket());
		}
	}

	private void validateTrackElementType(int elementType, String boundary) {
		if (elementType != 0 && elementType != 1 && elementType != 2 && elementType != 4) {
			throw new CrmParsingException("Unsupported " + boundary + " track element type: " + elementType);
		}
	}

	private void validateChannelMask(int channelMask, String source) {
		if (channelMask < 0 || channelMask > MAX_CHANNEL_MASK) {
			throw new CrmParsingException("Unsupported " + source + " channel mask: " + channelMask);
		}
	}

	private void validatePress(int press, String source) {
		if (press == 2 || press < 0 || press > 3) {
			throw new CrmParsingException("Unsupported " + source + " press value: " + press);
		}
	}

	private void validateIntertrackSpace(int intertrackSpace, String source) {
		if (intertrackSpace < 0 || intertrackSpace > MAX_INTERTRACK_SPACE) {
			throw new CrmParsingException("Unsupported " + source + " intertrack space: " + intertrackSpace);
		}
	}

	private void saveHardware(Long fragmentId, CrmFragmentHeader header) {
		CrmHardwareInfo mnk = header.hardwareMnk();
		CrmHardwareInfo system = header.hardwareSystem();
		CrmHardwareInfo rfid = header.hardwareRfid();
		CrmHardwareInfo rfb = header.hardwareRfb();
		CrmChannelCalibration step = header.stepCalibration();

		CorrectionFragmentHardware entity = new CorrectionFragmentHardware(
				fragmentId,
				(short) mnk.major(),
				(short) mnk.minor(),
				mnk.description(),
				(short) system.major(),
				(short) system.minor(),
				system.description(),
				(short) rfid.major(),
				(short) rfid.minor(),
				rfid.description(),
				(short) rfb.major(),
				(short) rfb.minor(),
				rfb.description(),
				step.msErrorTenth(),
				step.calibrationDate(),
				extractErrors(header.aoutCalibrations(), "AOUT"),
				extractDates(header.aoutCalibrations(), "AOUT"),
				extractErrors(header.ainCalibrations(), "AIN"),
				extractDates(header.ainCalibrations(), "AIN")
		);

		hardwareRepository.save(entity);
	}

	private short[] extractErrors(List<CrmChannelCalibration> calibrations, String name) {
		if (calibrations.size() != CALIBRATION_CHANNEL_COUNT) {
			throw new CrmParsingException("Invalid " + name + " calibration count: " + calibrations.size());
		}
		short[] errors = new short[CALIBRATION_CHANNEL_COUNT];
		for (int i = 0; i < CALIBRATION_CHANNEL_COUNT; i++) {
			errors[i] = calibrations.get(i).msErrorTenth();
		}
		return errors;
	}

	private LocalDate[] extractDates(List<CrmChannelCalibration> calibrations, String name) {
		if (calibrations.size() != CALIBRATION_CHANNEL_COUNT) {
			throw new CrmParsingException("Invalid " + name + " calibration count: " + calibrations.size());
		}
		LocalDate[] dates = new LocalDate[CALIBRATION_CHANNEL_COUNT];
		for (int i = 0; i < CALIBRATION_CHANNEL_COUNT; i++) {
			dates[i] = calibrations.get(i).calibrationDate();
		}
		return dates;
	}

	private void saveChannels(Long fragmentId, Map<Integer, byte[]> source) {
		if (source.isEmpty()) {
			return;
		}

		List<CorrectionFragmentChannel> channels = new ArrayList<>(source.size());
		for (Map.Entry<Integer, byte[]> entry : source.entrySet()) {
			int index = entry.getKey();
			if (index < 0 || index > 15) {
				throw new CrmParsingException("Invalid fragment channel index: " + index);
			}
			channels.add(new CorrectionFragmentChannel(fragmentId, (short) index, entry.getValue()));
		}

		channelRepository.saveAll(channels);
	}

	private void saveCycles(Long fragmentId, List<CrmPruCycle> source) {
		if (source.isEmpty()) {
			return;
		}

		List<CorrectionPruCycle> cycles = new ArrayList<>(source.size());
		for (CrmPruCycle item : source) {
			if (item.cycleDurationTenthSec() < 0 || item.cycleDurationTenthSec() > MAX_CYCLE_DURATION_TENTH_SEC) {
				throw new CrmParsingException("Unsupported PRU cycle duration: " + item.cycleDurationTenthSec());
			}
			if (item.cycleNumber() < 0 || item.cycleNumber() > MAX_CYCLE_NUMBER) {
				throw new CrmParsingException("Unsupported PRU cycle number: " + item.cycleNumber());
			}

			cycles.add(new CorrectionPruCycle(
					fragmentId,
					item.posStepTenths(),
					item.recordedTime(),
					(short) item.cycleDurationTenthSec(),
					(short) item.cycleNumber(),
					item.rightPress(),
					item.alignmentEnabled(),
					item.leftLiftEnabled(),
					item.rightLiftEnabled(),
					item.deltaAlignment(),
					item.deltaLeftLift(),
					item.deltaRightLift(),
					item.alertRegim(),
					item.alertSkipTrack(),
					item.alertRollback()
			));
		}

		pruCycleRepository.saveAll(cycles);
	}

	private void saveMarkers(Long fragmentId, List<MarkerData> source) {
		if (source.isEmpty()) {
			return;
		}

		List<CorrectionMarker> markers = new ArrayList<>(source.size());
		for (MarkerData item : source) {
			if (item.type() < 0 || item.type() > MAX_MARKER_TYPE_ID) {
				throw new CrmParsingException("Unsupported marker type: " + item.type());
			}
			markers.add(new CorrectionMarker(fragmentId, (short) item.type(), item.stepIndex()));
		}

		markerRepository.saveAll(markers);
	}

	private void saveRfids(Long fragmentId, List<CrmRfid> source) {
		if (source.isEmpty()) {
			return;
		}

		List<CorrectionRfid> rfids = new ArrayList<>(source.size());
		for (CrmRfid item : source) {
			if (item.blockMask() < 0 || item.blockMask() > MAX_RFID_BLOCK_MASK) {
				throw new CrmParsingException("Unsupported RFID block mask: " + item.blockMask());
			}
			if (item.pointCount() < 0 || item.pointCount() > MAX_RFID_POINT_COUNT) {
				throw new CrmParsingException("Unsupported RFID point count: " + item.pointCount());
			}

			rfids.add(new CorrectionRfid(
					fragmentId,
					item.timestampMs(),
					item.syncPulseCount(),
					item.tagIdentifier(),
					item.dataBlock(),
					item.odometerDeltaLenM(),
					item.odometerScaleAdj(),
					(short) item.blockMask(),
					(short) item.pointCount(),
					item.hasError(),
					item.controlDistanceM()
			));
		}

		rfidRepository.saveAll(rfids);
	}

	private void saveControlPoints(Long fragmentId, List<CrmControlPoint> source) {
		if (source.isEmpty()) {
			return;
		}

		List<CorrectionControlPoint> points = new ArrayList<>(source.size());
		for (CrmControlPoint item : source) {
			if (item.regimCode() < 0 || item.regimCode() > MAX_CTRL_REGIM_CODE) {
				throw new CrmParsingException("Unsupported control point regim code: " + item.regimCode());
			}
			if (item.tempCtrl() < 0 || item.tempCtrl() > MAX_CTRL_TEMP_CTRL) {
				throw new CrmParsingException("Unsupported control point tempCtrl: " + item.tempCtrl());
			}
			if (item.interfaceRegulators().length != 8 || item.externalRegulators().length != 5) {
				throw new CrmParsingException("Invalid control point regulator array size");
			}

			points.add(new CorrectionControlPoint(
					fragmentId,
					(short) item.regimCode(),
					(short) item.tempCtrl(),
					item.recordedTime(),
					item.interfaceRegulators(),
					item.externalRegulators(),
					item.leftRailTempC(),
					item.rightRailTempC(),
					item.airTempC()
			));
		}

		controlPointRepository.saveAll(points);
	}
}