package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCalculation;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtPoint;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CalculationParserTest {

	@Test
	void shouldParseAllRealExamples() throws Exception {
		CalculationParser parser = new CalculationParser();

		PrmCalculation calc1 = parser.parse(loadResource("/prm/prm.prm"));
		assertEquals(4755L, calc1.header().stepCount());
		assertNull(calc1.header().surveyTime());
		assertEquals(1, calc1.otvods().size());
		assertTrue(calc1.geoCoordinates().isEmpty());

		PrmCalculation calc2 = parser.parse(loadResource("/prm/prm2.prm"));
		assertEquals(2570L, calc2.header().stepCount());
		assertEquals(LocalDateTime.of(2018, 7, 9, 3, 32), calc2.header().surveyTime());
		assertEquals("DEVPC2", calc2.header().computerName());
		assertEquals(1, calc2.header().flags().press());
		assertEquals(Set.of(0, 1, 2, 3, 4, 5), calc2.channelData().keySet());

		PrmCalculation calc4 = parser.parse(loadResource("/prm/prm4.prm"));
		assertEquals(2192L, calc4.header().stepCount());
		assertEquals(LocalDateTime.of(2025, 9, 10, 14, 50), calc4.header().surveyTime());
		assertEquals("АЛГАИН - КАЛАРЫ", calc4.header().track().peregon().name());
		assertEquals((short) -32768, calc4.otvods().getFirst().deltaPlaneRightTenthMm());
	}

	@Test
	void shouldRejectTruncatedAndInvalidFiles() throws Exception {
		byte[] valid = loadResource("/prm/prm.prm");
		CalculationParser parser = new CalculationParser();

		byte[] truncated = Arrays.copyOf(valid, valid.length - 1);
		assertThrows(PrmParsingException.class, () -> parser.parse(truncated));

		byte[] badSignature = Arrays.copyOf(valid, valid.length);
		badSignature[0] = 'X';
		assertThrows(PrmParsingException.class, () -> parser.parse(badSignature));

		byte[] unsupportedVersion = Arrays.copyOf(valid, valid.length);
		unsupportedVersion[6] = 0x01;
		unsupportedVersion[7] = 0x08;
		assertThrows(PrmParsingException.class, () -> parser.parse(unsupportedVersion));
	}

	@Test
	void shouldParseVersion0605WithCdtAndGnss() {
		// Формирование синтетического файла версии 0x0605 с двумя осями CDT и GNSS
		int stepCount = 2;
		int totalSize = 0x300
				+ stepCount * Short.BYTES
				+ stepCount * 24 * 2
				+ 4 + stepCount * 24;

		ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);
		buf.put("NavPRM".getBytes(StandardCharsets.US_ASCII));
		buf.putShort((short) 0x0605);
		buf.putInt(packTime(2025, 5, 10, 12, 30));

		// Смещение к Flags для раскладки 06xx
		buf.position(87);
		// Бит 8 выставлен для MaskCH = 0x01
		buf.putShort((short) 0x0100);
		buf.putShort((short) 625);
		buf.putInt(stepCount);
		buf.putShort((short) 0);

		// Поле CDT на смещении 651 для раскладки 06xx
		buf.position(651);
		// BaseRail = 3, MaskPoints = 0xC (0x4 | 0x8) -> (0xC << 2) | 3 = 0x33
		buf.putShort((short) 0x0033);
		buf.putShort((short) 0);
		buf.putInt(0);

		// Каналы с 0x300 при нулевом InfoCH[0].Val интерпретируются как i16
		buf.position(0x300);
		buf.putShort((short) 10);
		buf.putShort((short) -20);

		// Измеренная ось Track_SRV
		buf.putDouble(1.0);
		buf.putDouble(2.0);
		buf.putDouble(3.0);
		buf.putDouble(4.0);
		buf.putDouble(5.0);
		buf.putDouble(6.0);

		// Проектная ось Track_PRJ
		buf.putDouble(10.0);
		buf.putDouble(20.0);
		buf.putDouble(30.0);
		buf.putDouble(40.0);
		buf.putDouble(50.0);
		buf.putDouble(60.0);

		// Секция GNSS
		buf.putInt(stepCount);
		buf.putDouble(55.0);
		buf.putDouble(83.0);
		buf.putDouble(120.0);
		buf.putDouble(55.1);
		buf.putDouble(83.1);
		buf.putDouble(121.0);

		PrmCalculation calc = new CalculationParser().parse(buf.array());

		assertEquals(0x0605, calc.header().version());
		assertEquals(1, calc.header().channelMask());
		assertEquals(2, calc.cdtData().measuredAxis().size());
		assertEquals(new PrmCdtPoint(1.0, 2.0, 3.0), calc.cdtData().measuredAxis().getFirst());
		assertEquals(new PrmCdtPoint(10.0, 20.0, 30.0), calc.cdtData().projectAxis().getFirst());
		assertEquals(2, calc.geoCoordinates().size());
		assertEquals(55.1, calc.geoCoordinates().get(1).latitude());
	}

	private int packTime(int year, int month, int day, int hours, int minutes) {
		return (year & 0xFFF)
				| ((month & 0xF) << 12)
				| ((day & 0x1F) << 16)
				| ((hours & 0x1F) << 21)
				| ((minutes & 0x3F) << 26);
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