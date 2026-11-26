package ru.ccfit.nsu.navigator.dataservice.parser.crm;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.PicketPosition;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmControlPoint;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmCorrection;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragment;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmFragmentHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.crm.model.CrmPruCycle;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CrmExampleTest {

	@Test
	void shouldParseAllRealExamples() throws Exception {
		CorrectionParser parser = new CorrectionParser();
		CrmFragmentDataParser dataParser = new CrmFragmentDataParser();

		// Проверка первого реального файла
		CrmCorrection crm = parser.parse(loadResource("/crm/crm.crm"));
		CrmHeader hdr1 = crm.header();
		assertEquals("NavCRM", hdr1.signature());
		assertEquals(0x0606, hdr1.version());
		assertEquals(LocalDateTime.of(2025, 3, 18, 10, 34), hdr1.correctionTime());
		assertEquals("Duomatic 09-32 CSM K", hdr1.machine().name());
		assertEquals(74L, hdr1.machine().number());
		assertEquals(4755L, hdr1.stepCount());
		assertEquals(1, hdr1.fragmentCount());
		assertEquals(5, hdr1.rfidCount());
		assertEquals("ТЯГУН - АЛАМБАЙ", hdr1.track().peregon().name());
		assertEquals(277, hdr1.paddingBytes().length);

		CrmFragment frag1 = crm.fragments().getFirst();
		CrmFragmentHeader fHdr1 = frag1.header();
		assertEquals(LocalDateTime.of(2025, 8, 26, 11, 34), fHdr1.startTime());
		assertEquals(LocalDateTime.of(2025, 8, 26, 11, 35), fHdr1.endTime());
		assertEquals(new PicketPosition(158, 1, 13.82f), new PicketPosition(
				fHdr1.startPicket().kilometer(),
				fHdr1.startPicket().picket(),
				Math.round(fHdr1.startPicket().meter() * 100.0f) / 100.0f
		));
		assertEquals(0, fHdr1.startPosStepTenths());
		assertEquals(2770, fHdr1.endPosStepTenths());
		assertEquals(278L, fHdr1.stepCount());
		assertEquals("Navi", fHdr1.software().type());
		assertEquals(7, fHdr1.software().major());
		assertEquals(5, fHdr1.software().minor());
		assertEquals("Ботвинко А.А. +7[12345]", fHdr1.operatorText());
		assertEquals(1, fHdr1.remarkMask());
		assertEquals("MNK", fHdr1.hardwareMnk().description());
		assertEquals(4, fHdr1.hardwareMnk().major());
		assertEquals(53, fHdr1.hardwareMnk().minor());
		assertEquals((short) 1000, fHdr1.stepCalibration().msErrorTenth());
		assertEquals(LocalDate.of(2025, 6, 3), fHdr1.stepCalibration().calibrationDate());
		assertNull(fHdr1.aoutCalibrations().getFirst().calibrationDate());
		assertEquals(LocalDate.of(2025, 8, 21), fHdr1.ainCalibrations().getFirst().calibrationDate());
		assertNull(fHdr1.ainCalibrations().get(7).calibrationDate());

		assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15), frag1.data().channelData().keySet());
		short[] prjPlaneY = dataParser.decodeShortValues(frag1.data().channelData().get(8));
		assertEquals(278, prjPlaneY.length);
		assertEquals(0, prjPlaneY[0]);
		assertEquals(2, prjPlaneY[1]);
		assertEquals(5, prjPlaneY[2]);
		assertEquals(7, frag1.data().cycles().size());
		assertEquals(278, frag1.data().controlPoints().size());

		CrmControlPoint ctrl0 = frag1.data().controlPoints().getFirst();
		assertEquals(974, ctrl0.regimCode());
		assertEquals(LocalTime.of(11, 34, 21), ctrl0.recordedTime());
		assertArrayEquals(new short[]{4, 2, 0, 0, 0}, ctrl0.externalRegulators());

		// Проверка второго реального файла
		CrmCorrection crm2 = parser.parse(loadResource("/crm/crm2.crm"));
		assertEquals(2570L, crm2.header().stepCount());
		CrmFragment frag2 = crm2.fragments().getFirst();
		assertEquals(2, frag2.data().cycles().size());
		CrmPruCycle cyc2 = frag2.data().cycles().getFirst();
		assertEquals(3978, cyc2.posStepTenths());
		assertEquals(8, cyc2.cycleDurationTenthSec());
		assertTrue(cyc2.rightPress());
		assertFalse(cyc2.alignmentEnabled());
		assertTrue(cyc2.rightLiftEnabled());

		// Проверка третьего реального файла и фильтрации цикла с CycleTime == 0
		CrmCorrection crm4 = parser.parse(loadResource("/crm/crm4.crm"));
		assertEquals(2192L, crm4.header().stepCount());
		assertEquals(44L, crm4.header().machine().number());
		CrmFragment frag4 = crm4.fragments().getFirst();
		assertEquals(1355, frag4.data().cycles().size());
		assertEquals(-9, frag4.data().cycles().getFirst().posStepTenths());
		assertEquals(21888, frag4.data().cycles().getLast().posStepTenths());
		assertEquals(2192, frag4.data().controlPoints().size());
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