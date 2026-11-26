package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.ChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.TrackData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmExtraExit;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmOtvod;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmSetup;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PrmExampleTest {

	@Test
	void shouldParseFirstExamplePrm() throws Exception {
		byte[] data = loadResource("/prm/prm.prm");
		BinaryReader reader = new BinaryReader(data, PrmParsingException::new);

		PrmHeader header = new PrmHeaderParser().parse(reader);
		assertEquals(0x300, reader.position());

		assertEquals("NavPRM", header.signature());
		assertEquals(0x0701, header.version());
		assertEquals(LocalDateTime.of(2025, 3, 18, 10, 34), header.calculationTime());
		assertNull(header.surveyTime());

		MachineData machine = header.machine();
		assertEquals(94, machine.rawType());
		assertEquals("Duomatic 09-32 CSM K", machine.name());
		assertEquals(74L, machine.number());
		assertArrayEquals(new int[]{0, 9855, 4715, 6015, 0}, machine.alignmentArms());
		assertArrayEquals(new int[]{0, 9820, 3985, 0, 0}, machine.levelingArms());
		assertArrayEquals(new int[]{890, 1515, 1560, 210}, machine.trolleyArms());
		assertEquals(0, machine.gaugeWidth());

		PrmExtraExit extraExit = header.extraExit();
		assertEquals(1617, extraExit.machinePos());
		assertEquals(1776, extraExit.planeFrontPos());
		assertEquals(3965171711L, extraExit.timeSeconds());
		assertTrue(extraExit.againstPicketage());
		assertFalse(extraExit.movingBackward());
		assertEquals(0, extraExit.status());

		PrmFlags flags = header.flags();
		assertTrue(flags.againstPicketage());
		assertFalse(flags.movingBackward());
		assertEquals(0, flags.press());
		assertFalse(flags.shortChord());
		assertFalse(flags.dlsEnabled());
		assertTrue(flags.warning());

		assertEquals(0x003F, header.channelMask());
		assertEquals(625, header.stepSizeMm());
		assertEquals(4755L, header.stepCount());
		assertEquals(1, header.otvodCount());
		assertEquals("DESKTOP-SLGF392", header.computerName());

		PrmSetup setup = header.setup();
		assertEquals(70, setup.passengerSpeed());
		assertEquals(60, setup.freightSpeed());
		assertEquals(50, setup.maxShiftLeftMm());
		assertEquals(50, setup.maxShiftRightMm());
		assertEquals(50, setup.maxLiftMm());
		assertEquals(10, setup.minLiftMm());

		TrackData track = header.track();
		assertEquals("1", track.trackNumber());
		assertEquals(155, track.begin().km());
		assertEquals((short) 86, track.begin().meters());
		assertEquals(158, track.end().km());
		assertEquals((short) 58, track.end().meters());
		assertEquals((short) 328, track.end().millimeters());
		assertEquals(83000378, track.peregon().sourceId());
		assertEquals("ТЯГУН - АЛАМБАЙ", track.peregon().name());

		ChannelInfo[] infos = header.channels();
		for (int i = 0; i <= 5; i++) {
			assertEquals(3, infos[i].valueType());
			assertEquals(1, infos[i].physicalType());
			assertEquals(10.0f, infos[i].scale());
		}

		assertArrayEquals(new byte[107], header.paddingBytes());

		PrmChannelDataParser channelParser = new PrmChannelDataParser();
		Map<Integer, byte[]> channels = channelParser.parse(reader, header);
		assertEquals(Set.of(0, 1, 2, 3, 4, 5), channels.keySet());
		assertEquals(9510, channels.get(0).length);

		short[] shiftValues = channelParser.decodeShortValues(channels.get(0));
		assertEquals(4755, shiftValues.length);
		assertEquals(0, shiftValues[0]);
		assertEquals(2, shiftValues[1]);
		assertEquals(5, shiftValues[2]);

		short[] planeChord = channelParser.decodeShortValues(channels.get(1));
		assertEquals(425, planeChord[0]);
		assertEquals(422, planeChord[1]);
		assertEquals(420, planeChord[2]);

		assertEquals(57828, reader.position());

		PrmCdtData cdtData = new PrmCdtParser().parse(reader, header);
		assertTrue(cdtData.measuredAxis().isEmpty());
		assertTrue(cdtData.projectAxis().isEmpty());

		List<PrmOtvod> otvods = new PrmOtvodParser().parse(reader, header.otvodCount());
		assertEquals(1, otvods.size());

		PrmOtvod otvod = otvods.getFirst();
		assertTrue(otvod.left());
		assertTrue(otvod.right());
		assertFalse(otvod.nonLinear());
		assertTrue(otvod.proportional());
		assertTrue(otvod.corrected());
		assertEquals(5, otvod.geometryMask());
		assertEquals(0, otvod.mode());
		assertEquals(0, otvod.source());
		assertFalse(otvod.autoByClearance());
		assertArrayEquals(new double[]{0.0, 0.0, 0.0, 0.0}, otvod.startPointsM());
		assertArrayEquals(new double[]{177.6, 189.6, 265.51996797437437, 277.51996797437437}, otvod.endPointsM());
		assertArrayEquals(new short[]{0, -1}, otvod.startInitialYTenthMm());
		assertArrayEquals(new short[]{0, 393}, otvod.endInitialYTenthMm());
		assertEquals((short) 0, otvod.deltaPlaneLeftTenthMm());
		assertEquals((short) 119, otvod.deltaPlaneRightTenthMm());
		assertEquals((short) 0, otvod.deltaProfileLeftTenthMm());
		assertEquals((short) 88, otvod.deltaProfileRightTenthMm());
		assertEquals(LocalDateTime.of(2025, 8, 26, 11, 35), otvod.creationTime());

		assertEquals(data.length, reader.position());
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