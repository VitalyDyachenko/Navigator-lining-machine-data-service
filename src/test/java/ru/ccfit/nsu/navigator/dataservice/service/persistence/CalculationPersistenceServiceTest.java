package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.Calculation;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationCdtPoint;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.calculation.CalculationOtvod;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.GeoCoordinate;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.CalculationParser;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCalculation;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtPoint;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.SurveyParser;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurvey;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationCdtPointRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationChannelRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationOtvodRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.calculation.CalculationRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculationPersistenceServiceTest {

	@Mock
	private CalculationRepository calculationRepository;
	@Mock
	private CalculationChannelRepository channelRepository;
	@Mock
	private CalculationOtvodRepository otvodRepository;
	@Mock
	private CalculationCdtPointRepository cdtPointRepository;
	@Mock
	private MachinePersistenceService machinePersistenceService;
	@Mock
	private GeoCoordinatePersistenceService geoCoordinatePersistenceService;

	private CalculationPersistenceService service;

	@BeforeEach
	void setUp() {
		service = new CalculationPersistenceService(
				calculationRepository,
				channelRepository,
				otvodRepository,
				cdtPointRepository,
				machinePersistenceService,
				geoCoordinatePersistenceService
		);
	}

	@Test
	void shouldSaveConsistentPairWithOtvodFlags() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv2.srv"));
		PrmCalculation calculation = new CalculationParser().parse(loadResource("/prm/prm2.prm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);

		service.save(10L, survey, calculation);

		verify(calculationRepository).saveAndFlush(any(Calculation.class));

		ArgumentCaptor<List<CalculationOtvod>> otvodsCaptor = ArgumentCaptor.forClass(List.class);
		verify(otvodRepository).saveAll(otvodsCaptor.capture());

		CalculationOtvod savedOtvod = otvodsCaptor.getValue().getFirst();
		assertTrue(savedOtvod.isPlane());
		assertFalse(savedOtvod.isLevel());
		assertTrue(savedOtvod.isProfile());

		verify(geoCoordinatePersistenceService).save(eq(10L), eq(true), eq(calculation.geoCoordinates()));
	}

	@Test
	void shouldAllowPairWithDifferentPicketBoundsAndSaveCalculationBounds() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv.srv"));
		PrmCalculation calculation = new CalculationParser().parse(loadResource("/prm/prm.prm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);

		service.save(1L, survey, calculation);

		ArgumentCaptor<Calculation> calculationCaptor = ArgumentCaptor.forClass(Calculation.class);
		verify(calculationRepository).saveAndFlush(calculationCaptor.capture());

		Calculation saved = calculationCaptor.getValue();
		assertEquals(155, saved.getStartKm());
		assertEquals((short) 86, saved.getStartM());
		assertEquals((short) 0, saved.getStartMm());
		assertEquals(158, saved.getEndKm());
		assertEquals((short) 58, saved.getEndM());
		assertEquals((short) 328, saved.getEndMm());
	}

	@Test
	void shouldRejectPairWithMismatchedTrackMetadata() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv4.srv"));
		PrmCalculation baseCalc = new CalculationParser().parse(loadResource("/prm/prm4.prm"));

		PrmHeader h = baseCalc.header();
		TrackData t = h.track();
		TrackData alteredTrack = new TrackData(
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
		PrmHeader badTrackHeader = new PrmHeader(
				h.signature(), h.version(), h.calculationTime(), h.machine(),
				h.extraExit(), h.flags(), h.channelMask(), h.stepSizeMm(), h.stepCount(),
				h.otvodCount(), h.setup(), h.surveyTime(), alteredTrack,
				h.computerName(), h.channels(), h.cdtHeader(), h.paddingBytes()
		);
		PrmCalculation badTrackCalc = new PrmCalculation(
				badTrackHeader, baseCalc.channelData(), baseCalc.cdtData(),
				baseCalc.otvods(), baseCalc.geoCoordinates()
		);

		assertThrows(PrmParsingException.class, () -> service.save(1L, survey, badTrackCalc));
		verifyNoInteractions(calculationRepository, channelRepository, otvodRepository);
	}
	@Test
	void shouldAllowNullTimeSrvAndRejectMismatchedTimeSrv() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv4.srv"));
		PrmCalculation baseCalc = new CalculationParser().parse(loadResource("/prm/prm4.prm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);

		PrmHeader baseHeader = baseCalc.header();
		PrmHeader nullTimeHeader = copyHeaderWithSurveyTime(baseHeader, null);
		PrmCalculation nullTimeCalc = new PrmCalculation(
				nullTimeHeader,
				baseCalc.channelData(),
				baseCalc.cdtData(),
				baseCalc.otvods(),
				baseCalc.geoCoordinates()
		);

		assertDoesNotThrow(() -> service.save(20L, survey, nullTimeCalc));

		PrmHeader wrongTimeHeader = copyHeaderWithSurveyTime(
				baseHeader,
				baseHeader.surveyTime().plusMinutes(1)
		);
		PrmCalculation wrongTimeCalc = new PrmCalculation(
				wrongTimeHeader,
				baseCalc.channelData(),
				baseCalc.cdtData(),
				baseCalc.otvods(),
				baseCalc.geoCoordinates()
		);

		assertThrows(PrmParsingException.class, () -> service.save(20L, survey, wrongTimeCalc));
	}

	@Test
	void shouldNormalizeZeroValChannelAndSaveCdtPointsAndSeparateMachineParams() throws Exception {
		SrvSurvey survey = new SurveyParser().parse(loadResource("/srv/srv4.srv"));
		PrmCalculation baseCalc = new CalculationParser().parse(loadResource("/prm/prm4.prm"));

		Machine machine = mock(Machine.class);
		MachineParams params = mock(MachineParams.class);
		when(machinePersistenceService.getOrCreateMachine(any())).thenReturn(machine);
		when(machinePersistenceService.getOrCreateMachineParams(eq(machine), any())).thenReturn(params);

		PrmHeader h = baseCalc.header();
		MachineData alteredParamsMachine = new MachineData(
				h.machine().rawType(),
				h.machine().machineType(),
				h.machine().name(),
				h.machine().number(),
				!h.machine().continuousCorrection(),
				h.machine().profileEnabled(),
				h.machine().planeSatellite(),
				h.machine().tampingSleepers(),
				new int[]{1, 2, 3, 4, 5},
				h.machine().levelingArms(),
				h.machine().trolleyArms(),
				1524
		);

		ChannelInfo[] modifiedInfos = h.channels().clone();
		modifiedInfos[0] = new ChannelInfo(0, 0, 0.0f);

		PrmHeader alteredHeader = new PrmHeader(
				h.signature(),
				h.version(),
				h.calculationTime(),
				alteredParamsMachine,
				h.extraExit(),
				h.flags(),
				h.channelMask(),
				h.stepSizeMm(),
				h.stepCount(),
				h.otvodCount(),
				h.setup(),
				h.surveyTime(),
				h.track(),
				h.computerName(),
				modifiedInfos,
				h.cdtHeader(),
				h.paddingBytes()
		);

		PrmCdtData cdt = new PrmCdtData(
				List.of(new PrmCdtPoint(1.0, 2.0, 3.0)),
				List.of(new PrmCdtPoint(4.0, 5.0, 6.0))
		);
		List<GeoCoordinate> geo = List.of(new GeoCoordinate(55.0, 83.0, 100.0));

		PrmCalculation modifiedCalc = new PrmCalculation(
				alteredHeader,
				Map.of(0, baseCalc.channelData().get(0)),
				cdt,
				baseCalc.otvods(),
				geo
		);

		service.save(30L, survey, modifiedCalc);

		verify(machinePersistenceService).getOrCreateMachineParams(machine, alteredParamsMachine);

		ArgumentCaptor<List<CalculationChannel>> channelsCaptor = ArgumentCaptor.forClass(List.class);
		verify(channelRepository).saveAll(channelsCaptor.capture());
		CalculationChannel ch0 = channelsCaptor.getValue().getFirst();
		assertEquals((short) 3, ch0.getValueType());
		assertEquals((short) 1, ch0.getPhysicalTypeId());
		assertEquals(10.0f, ch0.getScale());

		ArgumentCaptor<List<CalculationCdtPoint>> cdtCaptor = ArgumentCaptor.forClass(List.class);
		verify(cdtPointRepository).saveAll(cdtCaptor.capture());
		assertEquals(2, cdtCaptor.getValue().size());
		assertFalse(cdtCaptor.getValue().get(0).isProjectAxis());
		assertTrue(cdtCaptor.getValue().get(1).isProjectAxis());

		verify(geoCoordinatePersistenceService).save(30L, true, geo);
	}

	private PrmHeader copyHeaderWithSurveyTime(PrmHeader source, java.time.LocalDateTime newTime) {
		return new PrmHeader(
				source.signature(),
				source.version(),
				source.calculationTime(),
				source.machine(),
				source.extraExit(),
				source.flags(),
				source.channelMask(),
				source.stepSizeMm(),
				source.stepCount(),
				source.otvodCount(),
				source.setup(),
				newTime,
				source.track(),
				source.computerName(),
				source.channels(),
				source.cdtHeader(),
				source.paddingBytes()
		);
	}

	private byte[] loadResource(String path) throws Exception {
		try (InputStream input = getClass().getResourceAsStream(path)) {
			if (input == null) {
				throw new IllegalStateException("Test file not found: " + path);
			}
			return input.readAllBytes();
		}
	}
}