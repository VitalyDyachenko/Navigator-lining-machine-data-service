package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ccfit.nsu.navigator.dataservice.entity.correction.*;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.PicketPosition;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.SurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.marker.MarkerData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackPosition;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.CorrectionParser;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.SurveyParser;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.repository.correction.*;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrectionPersistenceServiceTest {

	@Mock
	private CorrectionRepository correctionRepository;
	@Mock
	private CorrectionFragmentRepository fragmentRepository;
	@Mock
	private CorrectionFragmentHardwareRepository hardwareRepository;
	@Mock
	private CorrectionFragmentChannelRepository channelRepository;
	@Mock
	private CorrectionPruCycleRepository pruCycleRepository;
	@Mock
	private CorrectionMarkerRepository markerRepository;
	@Mock
	private CorrectionRfidRepository rfidRepository;
	@Mock
	private CorrectionControlPointRepository controlPointRepository;
	@Mock
	private MachinePersistenceService machinePersistenceService;

	@Captor
	private ArgumentCaptor<List<CorrectionFragmentChannel>> channelListCaptor;
	@Captor
	private ArgumentCaptor<List<CorrectionPruCycle>> cycleListCaptor;
	@Captor
	private ArgumentCaptor<List<CorrectionControlPoint>> controlPointListCaptor;
	@Captor
	private ArgumentCaptor<List<CorrectionMarker>> markerListCaptor;
	@Captor
	private ArgumentCaptor<List<CorrectionRfid>> rfidListCaptor;

	private CorrectionPersistenceService service;

	@BeforeEach
	void setUp() {
		service = new CorrectionPersistenceService(
				correctionRepository,
				fragmentRepository,
				hardwareRepository,
				channelRepository,
				pruCycleRepository,
				markerRepository,
				rfidRepository,
				controlPointRepository,
				machinePersistenceService
		);
	}

	@Test
	void shouldSaveConsistentRealPairWithHardwareCalibrationsAndFragmentData() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv2.srv"));
		CrmCorrection correction = new CorrectionParser().parse(loadResource("/crm/crm2.crm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);
		when(fragmentRepository.save(any(CorrectionFragment.class))).thenAnswer(invocation -> {
			CorrectionFragment saved = spy(invocation.getArgument(0, CorrectionFragment.class));
			doReturn(101L).when(saved).getId();
			return saved;
		});

		service.save(10L, survey, correction);

		ArgumentCaptor<Correction> correctionCaptor = ArgumentCaptor.forClass(Correction.class);
		verify(correctionRepository).saveAndFlush(correctionCaptor.capture());
		Correction savedCorrection = correctionCaptor.getValue();
		assertEquals(157, savedCorrection.getStartKm());
		assertEquals((short) 22, savedCorrection.getStartM());
		assertEquals((short) 0, savedCorrection.getStartMm());
		assertEquals(158, savedCorrection.getEndKm());
		assertEquals((short) 627, savedCorrection.getEndM());
		assertEquals((short) 825, savedCorrection.getEndMm());

		verify(fragmentRepository).save(any(CorrectionFragment.class));

		ArgumentCaptor<CorrectionFragmentHardware> hwCaptor = ArgumentCaptor.forClass(CorrectionFragmentHardware.class);
		verify(hardwareRepository).save(hwCaptor.capture());
		CorrectionFragmentHardware savedHw = hwCaptor.getValue();
		assertEquals(101L, savedHw.getFragmentId());
		assertNull(savedHw.getAoutCalibrationDates()[0]);
		assertEquals(LocalDate.of(2025, 8, 21), savedHw.getAinCalibrationDates()[0]);
		assertNull(savedHw.getAinCalibrationDates()[7]);

		verify(channelRepository).saveAll(channelListCaptor.capture());
		assertEquals(15, channelListCaptor.getValue().size());

		verify(pruCycleRepository).saveAll(cycleListCaptor.capture());
		assertEquals(2, cycleListCaptor.getValue().size());

		verify(controlPointRepository).saveAll(controlPointListCaptor.capture());
		assertEquals(570, controlPointListCaptor.getValue().size());

		verifyNoInteractions(markerRepository, rfidRepository);
	}

	@Test
	void shouldSaveFourthRealPair() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv4.srv"));
		CrmCorrection correction = new CorrectionParser().parse(loadResource("/crm/crm4.crm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);
		when(fragmentRepository.save(any(CorrectionFragment.class))).thenAnswer(invocation -> {
			CorrectionFragment saved = spy(invocation.getArgument(0, CorrectionFragment.class));
			doReturn(202L).when(saved).getId();
			return saved;
		});

		service.save(40L, survey, correction);

		verify(pruCycleRepository).saveAll(cycleListCaptor.capture());
		assertEquals(1355, cycleListCaptor.getValue().size());
	}

	@Test
	void shouldSkipHardwareForVersion0501AndSaveMarkersAndRfids() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv2.srv"));
		CrmCorrection base = new CorrectionParser().parse(loadResource("/crm/crm2.crm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);
		when(fragmentRepository.save(any(CorrectionFragment.class))).thenAnswer(invocation -> {
			CorrectionFragment saved = spy(invocation.getArgument(0, CorrectionFragment.class));
			doReturn(303L).when(saved).getId();
			return saved;
		});

		CrmFragment baseFrag = base.fragments().getFirst();
		CrmFragmentHeader fh = baseFrag.header();
		CrmFragmentHeader legacyHeader = new CrmFragmentHeader(
				fh.machine(), fh.startTime(), fh.endTime(),
				fh.startPicket(), fh.endPicket(),
				fh.startPosStepTenths(), fh.endPosStepTenths(),
				fh.flags(), fh.channelMask(), fh.stepSizeMm(), fh.stepCount(),
				fh.cycleCount(), 1, 1, 0,
				fh.reservedXxxxCount(), fh.reserved0(), fh.blockSize(),
				fh.software(), fh.operatorText(),
				false, 0, 0, 0,
				fh.hardwareMnk(), fh.hardwareSystem(), fh.hardwareRfid(), fh.hardwareRfb(),
				fh.stepCalibration(), fh.aoutCalibrations(), fh.ainCalibrations(), fh.reservedTail()
		);

		CrmFragmentData legacyData = new CrmFragmentData(
				Map.of(0, baseFrag.data().channelData().get(0)),
				List.of(),
				List.of(new MarkerData(3, 10L)),
				List.of(new CrmRfid(0L, 100L, 42L, 7L, 12.5f, 1.0f, 3, 10, false, 5.0)),
				List.of()
		);

		CrmHeader h = base.header();
		CrmHeader header0501 = new CrmHeader(
				h.signature(), 0x0501, h.correctionTime(), h.machine(), h.flags(),
				h.channelMask(), h.stepSizeMm(), h.stepCount(), 1, h.rfidCount(),
				h.track(), h.paddingBytes()
		);

		CrmCorrection legacyCorrection = new CrmCorrection(
				header0501,
				List.of(new CrmFragment(legacyHeader, legacyData))
		);

		service.save(50L, survey, legacyCorrection);

		verifyNoInteractions(hardwareRepository);
		verify(markerRepository).saveAll(markerListCaptor.capture());
		assertEquals(1, markerListCaptor.getValue().size());
		verify(rfidRepository).saveAll(rfidListCaptor.capture());
		assertEquals(1, rfidListCaptor.getValue().size());
	}

	@Test
	void shouldRejectWorkMismatchesAndInvalidDbConstraints() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv2.srv"));
		CrmCorrection base = new CorrectionParser().parse(loadResource("/crm/crm2.crm"));
		CrmHeader h = base.header();

		// Несовпадение шага расчёта
		CrmHeader badStepSize = new CrmHeader(
				h.signature(), h.version(), h.correctionTime(), h.machine(), h.flags(),
				h.channelMask(), h.stepSizeMm() + 1, h.stepCount(), h.fragmentCount(),
				h.rfidCount(), h.track(), h.paddingBytes()
		);
		assertThrows(CrmParsingException.class, () ->
				service.save(1L, survey, new CrmCorrection(badStepSize, base.fragments())));

		// Несовпадение числа шагов
		CrmHeader badStepCount = new CrmHeader(
				h.signature(), h.version(), h.correctionTime(), h.machine(), h.flags(),
				h.channelMask(), h.stepSizeMm(), h.stepCount() + 1, h.fragmentCount(),
				h.rfidCount(), h.track(), h.paddingBytes()
		);
		assertThrows(CrmParsingException.class, () ->
				service.save(1L, survey, new CrmCorrection(badStepCount, base.fragments())));

		// Несовпадение машины во фрагменте
		CrmFragment baseFrag = base.fragments().getFirst();
		CrmFragmentHeader fh = baseFrag.header();
		MachineData wrongMachine = new MachineData(
				fh.machine().rawType(), fh.machine().machineType(), fh.machine().name(),
				fh.machine().number() + 1,
				fh.machine().continuousCorrection(), fh.machine().profileEnabled(),
				fh.machine().planeSatellite(), fh.machine().tampingSleepers(),
				fh.machine().alignmentArms(), fh.machine().levelingArms(),
				fh.machine().trolleyArms(), fh.machine().gaugeWidth()
		);
		CrmFragmentHeader wrongFragHeader = new CrmFragmentHeader(
				wrongMachine, fh.startTime(), fh.endTime(),
				fh.startPicket(), fh.endPicket(),
				fh.startPosStepTenths(), fh.endPosStepTenths(),
				fh.flags(), fh.channelMask(), fh.stepSizeMm(), fh.stepCount(),
				fh.cycleCount(), fh.markerCount(), fh.rfidCount(), fh.controlPointCount(),
				fh.reservedXxxxCount(), fh.reserved0(), fh.blockSize(),
				fh.software(), fh.operatorText(), fh.hasExtendedMetadata(),
				fh.remarkMask(), fh.startElementType(), fh.endElementType(),
				fh.hardwareMnk(), fh.hardwareSystem(), fh.hardwareRfid(), fh.hardwareRfb(),
				fh.stepCalibration(), fh.aoutCalibrations(), fh.ainCalibrations(), fh.reservedTail()
		);
		assertThrows(CrmParsingException.class, () ->
				service.save(1L, survey, new CrmCorrection(
						h, List.of(new CrmFragment(wrongFragHeader, baseFrag.data())))));

		// Запрещённое значение Press = 2
		SurveyFlags badPressFlags = new SurveyFlags(
				h.flags().againstPicketage(), h.flags().movingBackward(), 2,
				h.flags().shortChord(), h.flags().intertrackSpace(),
				h.flags().markerAtFront(), h.flags().dlsEnabled(), h.flags().reservedFlags()
		);
		CrmHeader badPressHeader = new CrmHeader(
				h.signature(), h.version(), h.correctionTime(), h.machine(), badPressFlags,
				h.channelMask(), h.stepSizeMm(), h.stepCount(), h.fragmentCount(),
				h.rfidCount(), h.track(), h.paddingBytes()
		);
		assertThrows(CrmParsingException.class, () ->
				service.save(1L, survey, new CrmCorrection(badPressHeader, base.fragments())));

		// Выход пикета фрагмента за границы 0..9
		CrmFragmentHeader badPicketHeader = new CrmFragmentHeader(
				fh.machine(), fh.startTime(), fh.endTime(),
				new PicketPosition(158, 10, 0.0f), fh.endPicket(),
				fh.startPosStepTenths(), fh.endPosStepTenths(),
				fh.flags(), fh.channelMask(), fh.stepSizeMm(), fh.stepCount(),
				fh.cycleCount(), fh.markerCount(), fh.rfidCount(), fh.controlPointCount(),
				fh.reservedXxxxCount(), fh.reserved0(), fh.blockSize(),
				fh.software(), fh.operatorText(), fh.hasExtendedMetadata(),
				fh.remarkMask(), fh.startElementType(), fh.endElementType(),
				fh.hardwareMnk(), fh.hardwareSystem(), fh.hardwareRfid(), fh.hardwareRfb(),
				fh.stepCalibration(), fh.aoutCalibrations(), fh.ainCalibrations(), fh.reservedTail()
		);
		assertThrows(CrmParsingException.class, () ->
				service.save(1L, survey, new CrmCorrection(
						h, List.of(new CrmFragment(badPicketHeader, baseFrag.data())))));

		verifyNoInteractions(hardwareRepository, channelRepository, pruCycleRepository);
	}

	private byte[] loadResource(String path) throws Exception {
		try (InputStream input = getClass().getResourceAsStream(path)) {
			if (input == null) {
				throw new IllegalStateException("Test file not found: " + path);
			}
			return input.readAllBytes();
		}
	}

	@Test
	void shouldAllowDifferentPicketBoundsAndRejectMismatchedTrackMetadata() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv2.srv"));
		CrmCorrection base = new CorrectionParser().parse(loadResource("/crm/crm2.crm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);
		when(fragmentRepository.save(any(CorrectionFragment.class))).thenAnswer(invocation -> {
			CorrectionFragment saved = spy(invocation.getArgument(0, CorrectionFragment.class));
			doReturn(404L).when(saved).getId();
			return saved;
		});

		CrmHeader h = base.header();
		TrackData t = h.track();
		TrackData differentBoundsTrack = new TrackData(
				t.trackNumber(),
				new TrackPosition(t.begin().km(), (short) (t.begin().meters() + 10), t.begin().millimeters()),
				t.end(),
				t.administrativeCode(),
				t.railway(),
				t.direction(),
				t.mainTrack(),
				t.peregon(),
				t.station1(),
				t.stationPosition1(),
				t.station2(),
				t.stationPosition2()
		);
		CrmHeader differentBoundsHeader = new CrmHeader(
				h.signature(), h.version(), h.correctionTime(), h.machine(), h.flags(),
				h.channelMask(), h.stepSizeMm(), h.stepCount(), h.fragmentCount(),
				h.rfidCount(), differentBoundsTrack, h.paddingBytes()
		);

		service.save(60L, survey, new CrmCorrection(differentBoundsHeader, base.fragments()));

		ArgumentCaptor<Correction> correctionCaptor = ArgumentCaptor.forClass(Correction.class);
		verify(correctionRepository).saveAndFlush(correctionCaptor.capture());
		assertEquals((short) (t.begin().meters() + 10), correctionCaptor.getValue().getStartM());

		TrackData mismatchedMetadataTrack = new TrackData(
				"99",
				t.begin(),
				t.end(),
				t.administrativeCode(),
				t.railway(),
				t.direction(),
				t.mainTrack(),
				t.peregon(),
				t.station1(),
				t.stationPosition1(),
				t.station2(),
				t.stationPosition2()
		);
		CrmHeader mismatchedHeader = new CrmHeader(
				h.signature(), h.version(), h.correctionTime(), h.machine(), h.flags(),
				h.channelMask(), h.stepSizeMm(), h.stepCount(), h.fragmentCount(),
				h.rfidCount(), mismatchedMetadataTrack, h.paddingBytes()
		);

		assertThrows(CrmParsingException.class, () ->
				service.save(60L, survey, new CrmCorrection(mismatchedHeader, base.fragments())));
	}
}