package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvMachine;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo.SrvChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

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

        SrvHeader header = parser.parse(reader);

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

        // tsTRACK — описание железнодорожного участка съёмки.
        SrvTrack track = new SrvTrackParser().parse(reader);

// NPUT — номер пути съёмки.
        assertEquals("1", track.trackNumber());

// PKT_BEGIN — начальная координата.
        assertEquals(3349, track.begin().km());
        assertEquals(577, track.begin().meters());
        assertEquals(0, track.begin().millimeters());

// PKT_END — конечная координата.
        assertEquals(3357, track.end().km());
        assertEquals(33, track.end().meters());
        assertEquals(0, track.end().millimeters());

// DOR — железная дорога.
        assertEquals("Западно-Сибирская", track.railway().name());
        assertEquals((short) 83, track.railway().code());

// UP — направление.
        assertEquals("Новосибирск-Красноярск", track.direction().name());
        assertEquals(840, track.direction().sourceId());

// PUTGL — главный путь.
        assertEquals(1235, track.mainTrack().sourceId());
        assertEquals("1", track.mainTrack().number());

// PEREG_MS — межстанционный перегон.
        assertEquals("Иня-восточная-Мочище", track.peregon().name());
        assertEquals(9.0, track.peregon().exploitationLength());

// STAN1 и STAN2 — станции.
        assertEquals(18929, track.station1().sourceId());
        assertEquals(18931, track.station2().sourceId());

// STANKM — позиция станции на главном пути.
        assertEquals(1235, track.stationPosition1().mainTrackSourceId());
        assertEquals(3348, track.stationPosition1().axisKm());
        assertEquals(788, track.stationPosition1().axisM());

// Конец структуры tsTRACK.
        assertEquals(491, reader.position());

        // tsVPI_H — информация о проезде и счётчики объектов.
        SrvVpiHeader vpi = new SrvVpiHeaderParser().parse(reader);

        assertEquals(59649L, vpi.startStep());
        assertEquals(1L, vpi.fragmentCount());
        assertEquals(8L, vpi.postCount());
        assertEquals(2L, vpi.switchCount());
        assertEquals(25L, vpi.defectCount());
        assertEquals(11902L, vpi.geoCoordCount());

        assertEquals(110, vpi.tripId().wagonNumber());
        assertEquals(2025, vpi.tripId().year());
        assertEquals(83, vpi.tripId().railwayCode());

        assertArrayEquals(new long[5], vpi.reserved());

// Конец tsVPI_H — 76 байт от смещения 491.
        assertEquals(567, reader.position());


        // InfoCH[16] — метаданные каналов.
        SrvChannelInfo[] channels = new SrvChannelInfoParser().parse(reader);

        assertEquals(16, channels.length);

// Каналы, у которых заданы метаданные.
        for (int index = 0; index <= 5; index++) {
            assertEquals(3, channels[index].valueType());
            assertEquals(1, channels[index].physicalType());
            assertEquals(10.0f, channels[index].scale());
        }

// Каналы без заполненных метаданных.
        for (int index = 6; index < 16; index++) {
            assertEquals(0, channels[index].valueType());
            assertEquals(0, channels[index].physicalType());
            assertEquals(0.0f, channels[index].scale());
        }

        assertEquals(663, reader.position());
    }
}