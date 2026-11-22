package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvMachine;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo.SrvChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SrvExampleTest {
    @Test
    void shouldParseHeader() throws Exception {

        byte[] data;

        try (InputStream input = getClass().getResourceAsStream("/srv/ivm.srv"))
        {
            if (input == null) {
                throw new IllegalStateException("Test file not found");
            }
            //TODO maybe to fix. readAllBytes need Integer not Long.
            data = input.readAllBytes();
        }

        SrvBinaryReader reader = new SrvBinaryReader(data);

        SrvHeaderParser parser = new SrvHeaderParser();

        SrvHeader header = parser.parse(reader);

        SrvTrack track = header.track();
        SrvVpiHeader vpi = header.vpi();
        SrvChannelInfo[] channels = header.channels();

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

        // tsVPI_H — информация о проезде и счётчики объектов.

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


        // InfoCH[16] — метаданные каналов.

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

        // Неописанная область заголовка: 105 байтов в ivm.srv.
        assertEquals(105, header.undocumentedHeaderBytes().length);

// Именно в нашем образце эта область заполнена нулями.
        assertArrayEquals(
                new byte[105],
                header.undocumentedHeaderBytes()
        );

// Предполагаемое начало блока измерений для ivm.srv версии 0x0605.
        assertEquals(768, reader.position());

        SrvChannelDataParser channelDataParser = new SrvChannelDataParser();

        Map<Integer, byte[]> data_ch = channelDataParser.parse(reader, header);

// MaskCH — должны быть извлечены все шесть активных каналов.
        assertEquals(Set.of(0, 1, 2, 5, 6, 7), data_ch.keySet());

// CntSTEP × sizeof(short) — размер каждого канала.
        for (byte[] channel : data_ch.values()) {
            assertEquals(23848, channel.length);
        }

// Проверяем преобразование в числа.
        short[] channel0 = channelDataParser.decodeShortValues(data_ch.get(0));

        assertEquals(11924, channel0.length);
        assertEquals(162, channel0[0]);
        assertEquals(154, channel0[1]);
        assertEquals(132, channel0[2]);

// Канал 6 содержит данные, несмотря на Val = 0.
        assertEquals(0, header.channels()[6].valueType());

        short[] channel6 = channelDataParser.decodeShortValues(data_ch.get(6));

        assertEquals(75, channel6[0]);
        assertEquals(76, channel6[1]);
        assertEquals(84, channel6[2]);

// Следующий блок — tsMARKER.
        assertEquals(143856, reader.position());


        // tsMARKER — маркеры съёмки.
        SrvMarkerParser markerParser = new SrvMarkerParser();

        List<SrvMarker> markers = markerParser.parse(
                reader,
                header.markerCount()
        );

        assertEquals(1, markers.size());

        SrvMarker marker = markers.getFirst();

// Type = 14 — стрелка, остряк.
        assertEquals(14, marker.type());

// iStep — индекс шага, на котором поставлен маркер.
        assertEquals(11924L, marker.stepIndex());

// Начало следующего блока tsRFID.
        assertEquals(143861, reader.position());

        // tsRFID — RFID-метки съёмки.
        SrvRfidParser rfidParser = new SrvRfidParser();

        List<SrvRfid> rfids = rfidParser.parse(reader, header.rfidCount());

        assertEquals(58, rfids.size());

// Первая RFID-метка из XLSX и ivm.srv.
        SrvRfid first = rfids.get(0);

        assertEquals(3349, first.kilometer());
        assertEquals(9, first.picket());
        assertEquals(0, first.markerType());

        assertEquals(526L, first.syncPulseCount());
        assertEquals(0xCD5595DCL, first.tagIdentifier());

        assertArrayEquals(new long[]{0, 0, 0}, first.dataBlocks());

        assertEquals(3, first.blockMask());
        assertEquals(1, first.pointCount());
        assertFalse(first.hasError());

        assertEquals(328.75, first.controlDistanceM());

// Последняя RFID-метка.
        SrvRfid last = rfids.get(57);

        assertEquals(3357, last.kilometer());
        assertEquals(0, last.picket());
        assertEquals(11873L, last.syncPulseCount());
        assertEquals(7420.625, last.controlDistanceM());

// Конец RFID, начало tsVPI_D.
        assertEquals(145833, reader.position());
    }
}