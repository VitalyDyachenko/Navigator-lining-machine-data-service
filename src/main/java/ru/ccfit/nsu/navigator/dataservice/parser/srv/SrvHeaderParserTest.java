package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeaderPrefix;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvMachine;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;

import java.io.InputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SrvHeaderParserTest {

    @Test
    void shouldParseHeader() throws Exception {

        byte[] data;

        try (InputStream input = getClass()
                .getResourceAsStream("/srv/ivm.srv")) {

            if (input == null) {
                throw new IllegalStateException("Test file not found");
            }

            data = input.readAllBytes();
        }

        SrvBinaryReader reader = new SrvBinaryReader(data);

        SrvHeaderParser parser = new SrvHeaderParser();

        SrvHeaderPrefix header = parser.parse(reader);

        assertEquals("NavSRV", header.signature());
        assertEquals(0x0605, header.version());
        assertEquals(
                LocalDateTime.of(2025, 1, 17, 14, 55),
                header.surveyTime()
        );

        SrvMachine machine = header.machine();

        assertEquals(1, machine.rawType());
        assertEquals(0, machine.machineType());
        assertEquals("КВЛ-П", machine.name());
        assertEquals(110L, machine.number());
        assertEquals(1520, machine.gaugeWidth());

        assertArrayEquals(
                new int[]{6740, 4100, 17400, 0, 0},
                machine.alignmentArms()
        );

        assertArrayEquals(
                new int[]{6740, 2700, 14300, 0, 0},
                machine.levelingArms()
        );

        assertArrayEquals(
                new int[]{0, 0, 0, 0},
                machine.trolleyArms()
        );

        SrvSurveyFlags flags = header.flags();

        assertFalse(flags.againstPicketage());
        assertFalse(flags.movingBackward());
        assertEquals(3, flags.press());
        assertFalse(flags.shortChord());
        assertEquals(0, flags.intertrackSpace());
        assertFalse(flags.markerAtFront());
        assertFalse(flags.dlsEnabled());
        assertEquals(0, flags.reservedFlags());

        assertEquals(0xE7, header.channelMask());
        assertEquals(625, header.stepSizeMm());
        assertEquals(11924L, header.stepCount());
        assertEquals(1, header.markerCount());
        assertEquals(58, header.rfidCount());

        assertEquals(85, reader.position());
    }
}