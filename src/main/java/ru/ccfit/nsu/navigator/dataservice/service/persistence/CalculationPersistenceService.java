package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.Calculation;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationCdtPoint;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationOtvod;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.PrmChannelDataParser;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCalculation;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtPoint;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmExtraExit;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmOtvod;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmSetup;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationCdtPointRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationChannelRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationOtvodRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CalculationPersistenceService {

	private static final int MASK_PLANE = 0x01;
	private static final int MASK_LEVEL = 0x02;
	private static final int MASK_PROFILE = 0x04;
	private static final int MAX_PHYSICAL_TYPE_ID = 5;
	private static final int MAX_OTVOD_MODE_ID = 4;
	private static final int MAX_OTVOD_SOURCE_ID = 3;

	private final CalculationRepository calculationRepository;
	private final CalculationChannelRepository channelRepository;
	private final CalculationOtvodRepository otvodRepository;
	private final CalculationCdtPointRepository cdtPointRepository;
	private final MachinePersistenceService machinePersistenceService;
	private final GeoCoordinatePersistenceService geoCoordinatePersistenceService;
	private final PrmChannelDataParser channelDataParser = new PrmChannelDataParser();

	public CalculationPersistenceService(
			CalculationRepository calculationRepository,
			CalculationChannelRepository channelRepository,
			CalculationOtvodRepository otvodRepository,
			CalculationCdtPointRepository cdtPointRepository,
			MachinePersistenceService machinePersistenceService,
			GeoCoordinatePersistenceService geoCoordinatePersistenceService
	) {
		this.calculationRepository = calculationRepository;
		this.channelRepository = channelRepository;
		this.otvodRepository = otvodRepository;
		this.cdtPointRepository = cdtPointRepository;
		this.machinePersistenceService = machinePersistenceService;
		this.geoCoordinatePersistenceService = geoCoordinatePersistenceService;
	}

	// Сохранение проекта с предварительной кросс-валидацией относительно съёмки
	public void save(Long workId, SrvSurvey survey, PrmCalculation calculation) {
		validateConsistency(survey, calculation);

		PrmHeader header = calculation.header();
		Machine machine = machinePersistenceService.getOrCreateMachine(header.machine());
		MachineParams params = machinePersistenceService.getOrCreateMachineParams(
				machine, header.machine()
		);

		saveCalculation(workId, params, header);
		saveChannels(workId, header, calculation.channelData());
		saveOtvods(workId, calculation.otvods());
		saveCdtPoints(workId, calculation.cdtData());
		geoCoordinatePersistenceService.save(workId, true, calculation.geoCoordinates());
	}

	// Сверка общих структур между файлами съёмки и проекта
	public void validateConsistency(SrvSurvey survey, PrmCalculation calculation) {
		SrvHeader srv = survey.header();
		PrmHeader prm = calculation.header();

		if (prm.stepSizeMm() != srv.stepSizeMm()) {
			throw new PrmParsingException(
					"Step size mismatch with SRV: PRM=" + prm.stepSizeMm()
							+ ", SRV=" + srv.stepSizeMm()
			);
		}

		if (prm.stepCount() != srv.stepCount()) {
			throw new PrmParsingException(
					"Step count mismatch with SRV: PRM=" + prm.stepCount()
							+ ", SRV=" + srv.stepCount()
			);
		}

		if (prm.surveyTime() != null && !prm.surveyTime().equals(srv.surveyTime())) {
			throw new PrmParsingException(
					"Survey time mismatch with SRV: PRM=" + prm.surveyTime()
							+ ", SRV=" + srv.surveyTime()
			);
		}

		validateMachineIdentity(srv.machine(), prm.machine());
		validateTrackConsistency(srv.track(), prm.track());
	}

	private void validateMachineIdentity(MachineData srvMachine, MachineData prmMachine) {
		if (srvMachine.machineType() != prmMachine.machineType()
				|| srvMachine.number() != prmMachine.number()
				|| !Objects.equals(srvMachine.name(), prmMachine.name())) {
			throw new PrmParsingException("Machine identity mismatch with SRV");
		}
	}

	private void validateTrackConsistency(TrackData srvTrack, TrackData prmTrack) {
		if (!Objects.equals(srvTrack.begin(), prmTrack.begin())
				|| !Objects.equals(srvTrack.end(), prmTrack.end())) {
			throw new PrmParsingException("Track bounds PKT_BEGIN/PKT_END mismatch with SRV");
		}

		if (!Objects.equals(srvTrack, prmTrack)) {
			throw new PrmParsingException("Track metadata mismatch with SRV");
		}
	}

	private void saveCalculation(Long workId, MachineParams params, PrmHeader header) {
		Calculation entity = new Calculation(
				workId,
				params,
				(short) header.version(),
				header.calculationTime(),
				header.computerName(),
				header.channelMask(),
				header.otvodCount()
		);

		PrmFlags flags = header.flags();
		validatePressValue(flags.press(), "header");
		entity.setFlags(
				flags.againstPicketage(),
				flags.movingBackward(),
				flags.press() == 0 || flags.press() == 3,
				flags.press() == 1 || flags.press() == 3,
				flags.shortChord(),
				flags.dlsEnabled(),
				flags.warning()
		);

		PrmSetup setup = header.setup();
		entity.setSetup(
				setup.passengerSpeed(),
				setup.freightSpeed(),
				setup.maxShiftLeftMm(),
				setup.maxShiftRightMm(),
				setup.maxLiftMm(),
				setup.minLiftMm()
		);

		PrmExtraExit extraExit = header.extraExit();
		validatePressValue(extraExit.press(), "extraExit");
		if (extraExit.status() < 0 || extraExit.status() > 3) {
			throw new PrmParsingException("Unsupported ExtraEXIT status: " + extraExit.status());
		}
		entity.setExtraExit(
				extraExit.machinePos(),
				extraExit.planeFrontPos(),
				extraExit.timeSeconds(),
				(short) extraExit.status(),
				extraExit.againstPicketage(),
				extraExit.movingBackward(),
				extraExit.press() == 0 || extraExit.press() == 3,
				extraExit.press() == 1 || extraExit.press() == 3,
				extraExit.shortChord(),
				(short) extraExit.intertrackSpace(),
				extraExit.markerAtFront(),
				extraExit.dlsEnabled()
		);

		PrmCdtHeader cdtHeader = header.cdtHeader();
		entity.setCdtHeader(
				(short) cdtHeader.baseRail(),
				(short) cdtHeader.pointsMask(),
				cdtHeader.baseOffsetMm(),
				cdtHeader.objectsCount()
		);

		calculationRepository.saveAndFlush(entity);
	}

	private void validatePressValue(int press, String source) {
		if (press == 2 || press < 0 || press > 3) {
			throw new PrmParsingException("Unsupported " + source + " press value: " + press);
		}
	}

	private void saveChannels(
			Long workId,
			PrmHeader header,
			Map<Integer, byte[]> source
	) {
		if (source.isEmpty()) {
			return;
		}

		ChannelInfo[] info = header.channels();
		List<CalculationChannel> channels = new ArrayList<>(source.size());

		for (Map.Entry<Integer, byte[]> entry : source.entrySet()) {
			int index = entry.getKey();
			if (index < 0 || index >= info.length || index > 15) {
				throw new PrmParsingException("Invalid channel index: " + index);
			}

			// Нормализация типов и масштаба для старого формата и нулевого Scale
			ChannelInfo normalized = channelDataParser.resolveEffectiveInfo(info[index]);
			if (normalized.physicalType() < 0 || normalized.physicalType() > MAX_PHYSICAL_TYPE_ID) {
				throw new PrmParsingException(
						"Unsupported channel physical type: " + normalized.physicalType()
				);
			}

			channels.add(new CalculationChannel(
					workId,
					(short) index,
					(short) normalized.valueType(),
					(short) normalized.physicalType(),
					normalized.scale(),
					entry.getValue()
			));
		}

		channelRepository.saveAll(channels);
	}

	private void saveOtvods(Long workId, List<PrmOtvod> source) {
		if (source.isEmpty()) {
			return;
		}

		List<CalculationOtvod> otvods = new ArrayList<>(source.size());

		for (int i = 0; i < source.size(); i++) {
			PrmOtvod item = source.get(i);

			if (item.mode() < 0 || item.mode() > MAX_OTVOD_MODE_ID) {
				throw new PrmParsingException("Unsupported otvod mode: " + item.mode());
			}
			if (item.source() < 0 || item.source() > MAX_OTVOD_SOURCE_ID) {
				throw new PrmParsingException("Unsupported otvod source: " + item.source());
			}

			int mask = item.geometryMask();
			otvods.add(new CalculationOtvod(
					workId,
					(short) i,
					item.left(),
					item.right(),
					item.nonLinear(),
					item.proportional(),
					item.corrected(),
					(mask & MASK_PLANE) != 0,
					(mask & MASK_LEVEL) != 0,
					(mask & MASK_PROFILE) != 0,
					(short) item.mode(),
					(short) item.source(),
					item.autoByClearance(),
					item.startPointsM(),
					item.endPointsM(),
					item.startCoefficients(),
					item.endCoefficients(),
					item.startInitialYTenthMm(),
					item.endInitialYTenthMm(),
					item.deltaPlaneLeftTenthMm(),
					item.deltaPlaneRightTenthMm(),
					item.deltaProfileLeftTenthMm(),
					item.deltaProfileRightTenthMm(),
					item.creationTime()
			));
		}

		otvodRepository.saveAll(otvods);
	}

	private void saveCdtPoints(Long workId, PrmCdtData cdtData) {
		List<PrmCdtPoint> measured = cdtData.measuredAxis();
		List<PrmCdtPoint> project = cdtData.projectAxis();

		if (measured.isEmpty() && project.isEmpty()) {
			return;
		}

		List<CalculationCdtPoint> points = new ArrayList<>(measured.size() + project.size());

		for (int i = 0; i < measured.size(); i++) {
			PrmCdtPoint point = measured.get(i);
			points.add(new CalculationCdtPoint(
					workId, i, false, point.x(), point.y(), point.h()
			));
		}

		for (int i = 0; i < project.size(); i++) {
			PrmCdtPoint point = project.get(i);
			points.add(new CalculationCdtPoint(
					workId, i, true, point.x(), point.y(), point.h()
			));
		}

		cdtPointRepository.saveAll(points);
	}
}