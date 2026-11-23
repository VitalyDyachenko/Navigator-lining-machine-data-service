package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import org.junit.jupiter.api.Test;
import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvMachine;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo.SrvChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvDefect;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvTrack;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SrvExampleTest {

    //============================ В этом тесте разбирается пример irm.srv
    @Test
    void shouldParseHeader() throws Exception {
        byte[] data;

        try (InputStream input = getClass().getResourceAsStream("/srv/ivm.srv")) {
            if (input == null) {
                throw new IllegalStateException("Test file not found");
            }
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

        // ===== tsVPI_H — информация о проезде и счётчики объектов =====

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

        // ===== InfoCH[16] — метаданные каналов =====

        assertEquals(16, channels.length);

        // Заполненные метаданные каналов.
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

        // Неописанная область заголовка в ivm.srv заполнена нулями.
        assertEquals(105, header.undocumentedHeaderBytes().length);

        assertArrayEquals(
                new byte[105],
                header.undocumentedHeaderBytes()
        );

        // Начало блока измерений в ivm.srv версии 0x0605.
        assertEquals(768, reader.position());

        SrvChannelDataParser channelDataParser = new SrvChannelDataParser();

        Map<Integer, byte[]> data_ch = channelDataParser.parse(reader, header);

        // MaskCH задаёт шесть активных каналов.
        assertEquals(Set.of(0, 1, 2, 5, 6, 7), data_ch.keySet());

        // Размер канала: CntSTEP × sizeof(short).
        for (byte[] channel : data_ch.values()) {
            assertEquals(23848, channel.length);
        }

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

        // ===== tsMARKER — маркеры съёмки =====
        SrvMarkerParser markerParser = new SrvMarkerParser();

        List<SrvMarker> markers = markerParser.parse(
                reader,
                header.markerCount(),
                header.stepCount()
        );

        assertEquals(1, markers.size());

        SrvMarker marker = markers.getFirst();

        // Type = 14 — стрелка, остряк.
        assertEquals(14, marker.type());

        // iStep — индекс шага, на котором поставлен маркер.
        assertEquals(11924L, marker.stepIndex());

        // Начало следующего блока tsRFID.
        assertEquals(143861, reader.position());

        // ===== tsRFID — RFID-метки съёмки =====
        SrvRfidParser rfidParser = new SrvRfidParser();

        List<SrvRfid> rfids = rfidParser.parse(reader, header.rfidCount());

        assertEquals(58, rfids.size());

        // Первая RFID-метка.
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

        SrvRfid last = rfids.get(57);

        assertEquals(3357, last.kilometer());
        assertEquals(0, last.picket());
        assertEquals(11873L, last.syncPulseCount());
        assertEquals(7420.625, last.controlDistanceM());

        assertEquals(145833, reader.position());

        assertEquals(145833, reader.position());

        SrvVpiDataParser vpiDataParser = new SrvVpiDataParser();

        // ===== tsFRAG — фрагмент съёмки =====

        // В ivm.srv один фрагмент съёмки.
        assertEquals(1L, header.vpi().fragmentCount());

        SrvFragment fragment = vpiDataParser.parseFragment(
                reader,
                header.vpi().fragmentCount()
        );

        // STP — начальный шаг фрагмента.
        assertEquals(0, fragment.startStepIndex());

        // KMN, MN — начальная координата.
        assertEquals(3349, fragment.startKm());
        assertEquals(577, fragment.startM());

        // KMK, MK — конечная координата.
        assertEquals(3357, fragment.endKm());
        assertEquals(33, fragment.endM());

        // Kod_napr — код направления или станции.
        assertEquals(13803L, fragment.directionStationCode());

        // Kod_park — код парка.
        assertEquals(0, fragment.parkCode());

        // Num_way — номер пути.
        assertEquals("1", fragment.trackNumber());

        // Hod — направление относительно пикетажа.
        assertEquals(1, fragment.picketDirection());

        // Name — название фрагмента.
        assertEquals("Иня-восточная-Мочище", fragment.name());

        assertEquals(145912, reader.position());

        // ===== tsPOST — километровые столбы =====

        // CntPOST — количество столбов из заголовка.
        assertEquals(8L, header.vpi().postCount());

        List<SrvPost> posts = vpiDataParser.parsePosts(
                reader,
                header.vpi().postCount()
        );

        assertEquals(8, posts.size());

        // Первый километровый столб.
        SrvPost firstPost = posts.get(0);

        // STP — шаг расположения столба.
        assertEquals(679, firstPost.stepIndex());

        // Number — номер километра.
        assertEquals(3350, firstPost.kilometerNumber());

        // isPasp — сведения из паспортных данных.
        assertTrue(firstPost.fromPassport());

        // Последний километровый столб.
        SrvPost lastPost = posts.get(7);

        assertEquals(11871, lastPost.stepIndex());
        assertEquals(3357, lastPost.kilometerNumber());
        assertTrue(lastPost.fromPassport());

        assertEquals(145984, reader.position());

        // ===== tsSWITCH — стрелочные переводы =====

        List<SrvSwitch> switches = vpiDataParser.parseSwitches(
                reader,
                header.vpi().switchCount()
        );

        assertEquals(2, switches.size());

        SrvSwitch firstSwitch = switches.getFirst();

        assertEquals(11924, firstSwitch.stepIndex());
        assertEquals(1, firstSwitch.direction());
        assertEquals(4, firstSwitch.type());
        assertEquals(31, firstSwitch.lengthM());
        assertEquals("2", firstSwitch.number());
        assertEquals(1074593444, firstSwitch.stationCode());
        assertTrue(firstSwitch.fromPassport());

        SrvSwitch secondSwitch = switches.get(1);

        assertEquals(11924, secondSwitch.stepIndex());
        assertEquals(1, secondSwitch.direction());
        assertEquals(4, secondSwitch.type());
        assertEquals(31, secondSwitch.lengthM());
        assertEquals("", secondSwitch.number());
        assertEquals(0, secondSwitch.stationCode());
        assertFalse(secondSwitch.fromPassport());

        assertEquals(146020, reader.position());

        // ===== tsBUILD — сооружения =====

        List<SrvBuild> builds = vpiDataParser.parseBuilds(
                reader,
                header.vpi().buildCount()
        );

        assertEquals(2, builds.size());

        SrvBuild firstBuild = builds.getFirst();

        assertEquals(294, firstBuild.startStepIndex());
        assertEquals(311, firstBuild.endStepIndex());
        assertEquals(5, firstBuild.type());
        assertTrue(firstBuild.fromPassport());

        SrvBuild secondBuild = builds.get(1);

        assertEquals(11366, secondBuild.startStepIndex());
        assertEquals(11383, secondBuild.endStepIndex());
        assertEquals(5, secondBuild.type());
        assertTrue(secondBuild.fromPassport());

        assertEquals(146042, reader.position());

        // ===== tsSPEED — установленные скорости =====

        List<SrvSpeed> speeds = vpiDataParser.parseSpeeds(
                reader,
                header.vpi().speedCount()
        );

        assertEquals(2, speeds.size());

        SrvSpeed firstSpeed = speeds.get(0);

        assertEquals(1, firstSpeed.startStepIndex());
        assertEquals(202, firstSpeed.endStepIndex());

        assertEquals(0, firstSpeed.startKm());
        assertEquals(0, firstSpeed.startPicket());
        assertEquals(0, firstSpeed.endKm());
        assertEquals(0, firstSpeed.endPicket());

        assertEquals(60, firstSpeed.passengerSpeed());
        assertEquals(60, firstSpeed.freightSpeed());
        assertEquals(60, firstSpeed.emptyTrainSpeed());

        SrvSpeed secondSpeed = speeds.get(1);

        assertEquals(202, secondSpeed.startStepIndex());
        assertEquals(11924, secondSpeed.endStepIndex());

        assertEquals(0, secondSpeed.startKm());
        assertEquals(0, secondSpeed.startPicket());
        assertEquals(0, secondSpeed.endKm());
        assertEquals(0, secondSpeed.endPicket());

        assertEquals(100, secondSpeed.passengerSpeed());
        assertEquals(80, secondSpeed.freightSpeed());
        assertEquals(80, secondSpeed.emptyTrainSpeed());

        assertEquals(146086, reader.position());

        // ===== tsTHREAD — участки левой рихтовочной нити =====

        List<SrvThread> threads = vpiDataParser.parseThreads(
                reader,
                header.vpi().threadCount()
        );

        assertEquals(2, threads.size());

        SrvThread firstThread = threads.get(0);

        assertEquals(25, firstThread.startStepIndex());
        assertEquals(606, firstThread.endStepIndex());

        SrvThread secondThread = threads.get(1);

        assertEquals(1479, secondThread.startStepIndex());
        assertEquals(11924, secondThread.endStepIndex());

        // Конец tsTHREAD, начало tsUROV.
        assertEquals(146102, reader.position());

        // ===== tsUROV — точки возвышения =====

        List<SrvElevation> elevations = vpiDataParser.parseElevations(
                reader,
                header.vpi().elevationCount()
        );

        assertEquals(15, elevations.size());

        SrvElevation firstElevation = elevations.get(0);

        assertEquals(1, firstElevation.stepIndex());

        // Знак нуля должен сохраняться при чтении.
        assertEquals(
                Float.floatToRawIntBits(-0.0f),
                Float.floatToRawIntBits(firstElevation.valueMm())
        );

        // Точка с возвышением +120 мм.
        SrvElevation positiveElevation = elevations.get(2);

        assertEquals(757, positiveElevation.stepIndex());
        assertEquals(120.0f, positiveElevation.valueMm());

        // Точка с возвышением -60 мм.
        SrvElevation negativeElevation = elevations.get(6);

        assertEquals(5162, negativeElevation.stepIndex());
        assertEquals(-60.0f, negativeElevation.valueMm());

        // Точка с возвышением -40 мм.
        SrvElevation anotherElevation = elevations.get(10);

        assertEquals(9204, anotherElevation.stepIndex());
        assertEquals(-40.0f, anotherElevation.valueMm());

        SrvElevation lastElevation = elevations.get(14);

        assertEquals(11924, lastElevation.stepIndex());
        assertEquals(
                Float.floatToRawIntBits(-0.0f),
                Float.floatToRawIntBits(lastElevation.valueMm())
        );

        // Конец tsUROV, начало tsCURVE.
        assertEquals(146222, reader.position());

        // ===== tsCURVE — параметры кривых пути =====

        List<SrvCurve> curves = vpiDataParser.parseCurves(
                reader,
                header.vpi().curveCount()
        );

        assertEquals(15, curves.size());

        SrvCurve firstCurve = curves.get(0);

        assertEquals(1, firstCurve.stepIndex());
        assertEquals(1520.0f, firstCurve.gaugeNormMm());
        assertEquals(0.0f, firstCurve.wearMm());
        assertEquals(0.0f, firstCurve.toleranceMm());

        // RI содержит отрицательный ноль.
        assertEquals(
                Float.floatToRawIntBits(-0.0f),
                Float.floatToRawIntBits(firstCurve.radiusM())
        );

        // Точка с радиусом 638 метров.
        SrvCurve positiveCurve = curves.get(2);

        assertEquals(757, positiveCurve.stepIndex());
        assertEquals(638.0f, positiveCurve.radiusM());
        assertEquals(1520.0f, positiveCurve.gaugeNormMm());
        assertEquals(2.0f, positiveCurve.wearMm());
        assertEquals(0.0f, positiveCurve.toleranceMm());

        // Точка с отрицательным радиусом.
        SrvCurve negativeCurve = curves.get(6);

        assertEquals(5162, negativeCurve.stepIndex());
        assertEquals(-1080.0f, negativeCurve.radiusM());
        assertEquals(1520.0f, negativeCurve.gaugeNormMm());
        assertEquals(2.0f, negativeCurve.wearMm());

        // Точка с радиусом -1250 м.
        SrvCurve anotherCurve = curves.get(10);

        assertEquals(9204, anotherCurve.stepIndex());
        assertEquals(-1250.0f, anotherCurve.radiusM());

        SrvCurve lastCurve = curves.get(14);

        assertEquals(11924, lastCurve.stepIndex());
        assertEquals(1520.0f, lastCurve.gaugeNormMm());
        assertEquals(0.0f, lastCurve.wearMm());
        assertEquals(0.0f, lastCurve.toleranceMm());

        assertEquals(146522, reader.position());

        // ===== tsDEFECT — неисправности пути =====

        List<SrvDefect> defects = vpiDataParser.parseDefects(
                reader,
                header.vpi().defectCount()
        );

        assertEquals(25, defects.size());

        // Первая неисправность — рихтовка по правому борту.
        SrvDefect firstDefect = defects.get(0);

        assertEquals(0, firstDefect.startStepIndex());
        assertEquals(53, firstDefect.endStepIndex());
        assertEquals(1, firstDefect.mask());
        assertEquals(28.0f, firstDefect.valueMm());
        assertEquals(3116, firstDefect.typeCode());

        assertEquals(65535, firstDefect.passengerSpeed());
        assertEquals(65535, firstDefect.freightSpeed());
        assertEquals(65535, firstDefect.emptyTrainSpeed());

        assertEquals(1, firstDefect.degree());
        assertEquals("Р", firstDefect.label());

        // Вторая неисправность — рихтовка по левому борту.
        SrvDefect secondDefect = defects.get(1);

        assertEquals(37, secondDefect.startStepIndex());
        assertEquals(64, secondDefect.endStepIndex());
        assertEquals(1, secondDefect.mask());
        assertEquals(21.0f, secondDefect.valueMm());
        assertEquals(3117, secondDefect.typeCode());
        assertEquals(2, secondDefect.degree());
        assertEquals("Р", secondDefect.label());

        // Пятая неисправность — перекос.
        SrvDefect fifthDefect = defects.get(4);

        assertEquals(839, fifthDefect.startStepIndex());
        assertEquals(870, fifthDefect.endStepIndex());
        assertEquals(2, fifthDefect.mask());
        assertEquals(10.0f, fifthDefect.valueMm());
        assertEquals(3115, fifthDefect.typeCode());
        assertEquals("П", fifthDefect.label());

        // Просадка по левому борту.
        SrvDefect subsidence = defects.get(20);

        assertEquals(11366, subsidence.startStepIndex());
        assertEquals(11376, subsidence.endStepIndex());
        assertEquals(4, subsidence.mask());
        assertEquals(10.0f, subsidence.valueMm());
        assertEquals(3119, subsidence.typeCode());
        assertEquals("Пр", subsidence.label());

        SrvDefect lastDefect = defects.get(24);

        assertEquals(11891, lastDefect.startStepIndex());
        assertEquals(11907, lastDefect.endStepIndex());
        assertEquals(3115, lastDefect.typeCode());
        assertEquals(9.0f, lastDefect.valueMm());
        assertEquals(1, lastDefect.degree());

        assertEquals(147272, reader.position());

        // ===== tsGABARIT — габариты обустройства пути =====

        // CntGABARIT — в ivm.srv габаритов нет.
        List<SrvGabarit> gabarits = vpiDataParser.parseGabarits(
                reader,
                header.vpi().gabaritCount()
        );

        assertTrue(gabarits.isEmpty());

        // Пустой блок не меняет позицию чтения.
        assertEquals(147272, reader.position());

        // ===== tsGEO_COORD — GNSS-координаты оси пути =====

        List<SrvGeoCoordinate> coordinates =
                vpiDataParser.parseGeoCoordinates(
                        reader,
                        header.vpi().geoCoordCount()
                );

        assertEquals(11902, coordinates.size());

        // Первая GNSS-точка.
        SrvGeoCoordinate first_geo = coordinates.get(0);

        assertEquals(55.11728140147731, first_geo.latitude());
        assertEquals(83.00798073517092, first_geo.longitude());
        assertEquals(0.0, first_geo.altitude());

        // Вторая GNSS-точка.
        SrvGeoCoordinate second = coordinates.get(1);

        assertEquals(55.11728770953188, second.latitude());
        assertEquals(83.0079863212119, second.longitude());
        assertEquals(0.0, second.altitude());

        // Последняя GNSS-точка.
        SrvGeoCoordinate last_geo = coordinates.get(11901);

        assertEquals(55.152011147838394, last_geo.latitude());
        assertEquals(83.10564274661138, last_geo.longitude());
        assertEquals(0.0, last_geo.altitude());

        // Файл прочитан до конца.
        assertEquals(432920, reader.position());
        assertEquals(data.length, reader.position());
    }
}
