package ru.ccfit.nsu.navigator.dataservice.parser.crm;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.CrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmCorrection;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragment;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmPruCycle;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmRfid;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CorrectionParserTest {

	@Test
	void shouldParseEmptyFileWithoutFragments() {
		ByteBuffer buf = ByteBuffer.allocate(0x300).order(ByteOrder.LITTLE_ENDIAN);
		buf.put("NavCRM".getBytes(StandardCharsets.US_ASCII));
		buf.putShort((short) 0x0606);
		buf.putInt(0);
		buf.position(75);
		buf.putShort((short) 625);
		buf.putInt(100);
		buf.putShort((short) 0);

		CrmCorrection correction = new CorrectionParser().parse(buf.array());
		assertNull(correction.header().correctionTime());
		assertTrue(correction.fragments().isEmpty());
	}

	@Test
	void shouldParseVersion0501WithLegacyPicketAndRfidAndCycleFiltering() {
		int stepCount = 2;
		int blockSize = stepCount * Short.BYTES + 2 * 16 + 5 + 34;
		int totalSize = 0x300 + 256 + blockSize;

		ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);
		buf.put("NavCRM".getBytes(StandardCharsets.US_ASCII));
		buf.putShort((short) 0x0501);
		buf.putInt(packTime(2024, 6, 15, 10, 0));

		// Установка Mash.Type = 1 для принудительного NavigatorVPI
		buf.put((byte) 1);

		buf.position(75);
		buf.putShort((short) 625);
		buf.putInt(50);
		buf.putShort((short) 1);

		// Описатель фрагмента 0x0501 длиной 256 байт
		int fragOffset = 0x300;
		buf.position(fragOffset + 59);
		buf.putInt(packTime(2024, 6, 15, 10, 5));
		// Невалидное время финиша для проверки пересчёта при StartTime != null
		buf.putInt((13 << 12) | 2024);

		// Устаревший пикетаж tsPKT для старта и финиша
		buf.putInt(-5);
		buf.putShort((short) 345);
		buf.putShort((short) 500);
		buf.putInt(10);
		buf.putShort((short) 25);
		buf.putShort((short) 0);

		// Обратный порядок IdPOS для проверки нормализации и пересчёта времени финиша
		buf.putInt(180);
		buf.putInt(0);

		buf.putShort((short) 0x4000);
		buf.putShort((short) 0x0001);
		buf.putShort((short) 625);
		buf.putInt(stepCount);
		buf.putShort((short) 2);
		buf.putShort((short) 1);
		buf.putShort((short) 1);
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putInt(0);
		buf.putInt(blockSize);

		buf.position(fragOffset + 215);
		buf.put((byte) 1);
		buf.put((byte) 2);
		buf.put((byte) 5);
		buf.put((byte) 6);
		buf.put("Navi".getBytes(StandardCharsets.US_ASCII));
		buf.put("Оператор".getBytes(java.nio.charset.Charset.forName("windows-1251")));

		// Блок данных фрагмента
		buf.position(0x300 + 256);
		buf.putShort((short) 15);
		buf.putShort((short) -25);

		// Валидный цикл ПРУ
		buf.putInt(100);
		buf.putInt(packCycleWord(10, 15, 20, 12, 2, true, true, false, true));
		buf.putShort((short) 1);
		buf.putShort((short) 2);
		buf.putShort((short) 3);
		buf.putShort((short) 0b101);

		// Отбрасываемый цикл с IdPOS / 10 > Header.CntSTEP
		buf.putInt(600);
		buf.putInt(packCycleWord(10, 16, 0, 10, 1, false, false, false, false));
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0);

		// Маркер
		buf.put((byte) 3);
		buf.putInt(1);

		// Устаревшая метка tsRFID
		buf.putInt(200);
		buf.putInt(3000);
		buf.putInt(42);
		buf.putInt(7);
		buf.putFloat(0.0f);
		buf.putFloat(1.02f);
		buf.putShort((short) 0x8053);
		buf.putDouble(12.5);

		CrmCorrection result = new CorrectionParser().parse(buf.array());
		assertEquals(0x0501, result.header().version());
		assertEquals(1, result.fragments().size());

		CrmFragment fragment = result.fragments().getFirst();
		assertEquals(0, fragment.header().startPosStepTenths());
		assertEquals(180, fragment.header().endPosStepTenths());
		assertEquals(LocalDateTime.of(2024, 6, 15, 10, 6), fragment.header().endTime());
		assertFalse(fragment.header().flags().setupMark());
		assertTrue(fragment.header().flags().navigatorVpi());
		assertFalse(fragment.header().hasExtendedMetadata());

		assertEquals(0, fragment.header().startPicket().kilometer());
		assertEquals(3, fragment.header().startPicket().picket());
		assertEquals(45.5f, fragment.header().startPicket().meter(), 0.001f);

		assertEquals(1, fragment.data().cycles().size());
		CrmPruCycle cycle = fragment.data().cycles().getFirst();
		assertEquals(100, cycle.posStepTenths());
		assertTrue(cycle.alertRegim());
		assertFalse(cycle.alertSkipTrack());
		assertTrue(cycle.alertRollback());

		assertEquals(1, fragment.data().markers().size());
		CrmRfid rfid = fragment.data().rfids().getFirst();
		assertEquals(0L, rfid.timestampMs());
		assertEquals(12.5f, rfid.odometerDeltaLenM(), 0.001f);
		assertTrue(rfid.hasError());
	}

	@Test
	void shouldParseEmptyFragmentAndSkipTimeRecalculationInDecember() {
		int totalSize = 0x300 + 512;
		ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);
		buf.put("NavCRM".getBytes(StandardCharsets.US_ASCII));
		buf.putShort((short) 0x0606);
		buf.putInt(0);
		buf.position(75);
		buf.putShort((short) 625);
		buf.putInt(100);
		buf.putShort((short) 1);

		// Фрагмент с CntSTEP = 0 и SizeBLOCK = 0 в декабре
		int fragOffset = 0x300;
		buf.position(fragOffset + 59);
		buf.putInt(packTime(2024, 12, 31, 23, 55));
		buf.putInt(packTime(2025, 1, 1, 0, 5));

		CrmCorrection correction = new CorrectionParser().parse(buf.array());
		CrmFragment fragment = correction.fragments().getFirst();
		assertEquals(LocalDateTime.of(2025, 1, 1, 0, 5), fragment.header().endTime());
		assertTrue(fragment.data().channelData().isEmpty());
	}

	@Test
	void shouldRejectInvalidAndTruncatedFiles() throws Exception {
		byte[] valid = loadResource("/crm/crm.crm");
		CorrectionParser parser = new CorrectionParser();

		assertThrows(CrmParsingException.class, () -> parser.parse(Arrays.copyOf(valid, valid.length - 1)));

		byte[] badSig = Arrays.copyOf(valid, valid.length);
		badSig[0] = 'X';
		assertThrows(CrmParsingException.class, () -> parser.parse(badSig));

		byte[] badVer = Arrays.copyOf(valid, valid.length);
		badVer[6] = 0x01;
		badVer[7] = 0x07;
		assertThrows(CrmParsingException.class, () -> parser.parse(badVer));

		byte[] badBlockSize = Arrays.copyOf(valid, valid.length);
		badBlockSize[0x300 + 115] += 1;
		assertThrows(CrmParsingException.class, () -> parser.parse(badBlockSize));
	}

	private int packTime(int year, int month, int day, int hours, int minutes) {
		return (year & 0xFFF)
				| ((month & 0xF) << 12)
				| ((day & 0x1F) << 16)
				| ((hours & 0x1F) << 21)
				| ((minutes & 0x3F) << 26);
	}

	private int packCycleWord(
			int hours,
			int minutes,
			int seconds,
			int cycleTimeTenthSec,
			int cycleNumber,
			boolean rightPress,
			boolean align,
			boolean leftLift,
			boolean rightLift
	) {
		return (hours & 0x1F)
				| ((minutes & 0x3F) << 5)
				| ((seconds & 0x3F) << 11)
				| ((cycleTimeTenthSec & 0x7F) << 17)
				| ((cycleNumber & 0x3) << 24)
				| ((rightPress ? 1 : 0) << 27)
				| ((align ? 1 : 0) << 28)
				| ((leftLift ? 1 : 0) << 29)
				| ((rightLift ? 1 : 0) << 30);
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