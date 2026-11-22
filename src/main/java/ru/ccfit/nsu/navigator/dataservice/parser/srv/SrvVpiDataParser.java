package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvDefect;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class SrvVpiDataParser {
    private static final Charset CP1251 =  Charset.forName("windows-1251");
    private static final int SWITCH_SIZE = 18;
    private static final int BUILD_SIZE = 11;
    private static final int FRAGMENT_SIZE = 79;
    private static final int POST_SIZE = 9;
    private static final int SPEED_SIZE = 22;
    private static final int THREAD_SIZE = 8;
    private static final int ELEVATION_SIZE = 8;
    private static final int CURVE_SIZE = 20;
    private static final int DEFECT_SIZE = 30;
    private static final int GABARIT_SIZE = 96;
    private static final int GEO_COORD_SIZE = 24;

    public SrvFragment parseFragment(SrvBinaryReader reader, long fragmentCount)
    {
        // CntFRAG — количество фрагментов в файле.
        if (fragmentCount != 1) {
            throw new SrvParsingException("Expected exactly one fragment, got: " + fragmentCount);
        }

        // tsFRAG — проверяем наличие 79 байт для фрагмента.
        checkCount(reader, fragmentCount, FRAGMENT_SIZE);

        int startStepIndex = reader.readInt(); // STP — начальный шаг фрагмента

        short startKm = reader.readShort(); // KMN — километр начала
        short startM = reader.readShort();  // MN — метры начала

        short endKm = reader.readShort();   // KMK — километр конца
        short endM = reader.readShort();    // MK — метры конца

        long directionStationCode = reader.readUnsignedInt();
        // Kod_napr — код направления или станции

        int parkCode = reader.readUnsignedByte();
        // Kod_park — код парка; 0 — главный путь или перегон

        String trackNumber = readString(reader, 10);
        // Num_way[10] — номер пути

        int picketDirection = reader.readUnsignedByte();
        // Hod — направление относительно пикетажа

        String name = readString(reader, 51);
        // Name[51] — название фрагмента

        return new SrvFragment(
                startStepIndex,
                startKm,
                startM,
                endKm,
                endM,
                directionStationCode,
                parkCode,
                trackNumber,
                picketDirection,
                name
        );
    }


    // CntPOST — количество километровых столбов из tsVPI_H.
    public List<SrvPost> parsePosts(
            SrvBinaryReader reader, long count
    ) {
        checkCount(reader, count, POST_SIZE);

        List<SrvPost> posts = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int stepIndex = reader.readInt();
            // STP — шаг расположения столба

            int kilometerNumber = reader.readInt();
            // Number — номер километра

            int passportFlag = reader.readUnsignedByte();
            // isPasp — 1 паспортные, 0 фактические данные

            if (passportFlag != 0 && passportFlag != 1) {
                throw new SrvParsingException(
                        "Invalid isPasp value: " + passportFlag
                );
            }

            posts.add(new SrvPost(
                    stepIndex,
                    kilometerNumber,
                    passportFlag == 1
            ));
        }

        return posts;
    }

    // Проверяем, что заявленное количество записей
    // физически помещается в оставшейся части файла.
    private void checkCount(SrvBinaryReader reader, long count, int recordSize) {
        if (count < 0 || count > reader.remaining() / recordSize) {
            throw new SrvParsingException("Invalid VPI record count: " + count);
        }
    }

    // Строка фиксированной длины, Windows-1251, до нулевого байта.
    private String readString(SrvBinaryReader reader, int length) {
        byte[] bytes = reader.readBytes(length);

        int end = 0;
        while (end < bytes.length && bytes[end] != 0) {
            end++;
        }

        return new String(bytes, 0, end, CP1251).trim();
    }

    public List<SrvSwitch> parseSwitches(
            SrvBinaryReader reader,
            long count
    ) {
        // CntSWITCH — количество стрелочных переводов.
        checkCount(reader, count, SWITCH_SIZE);

        List<SrvSwitch> switches = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int stepIndex = reader.readInt();
            // STP — индекс шага, расположение стрелки

            int direction = reader.readUnsignedByte();
            // Napr — 0 неизвестно, 1 пошерстная, 2 противошерстная

            int type = reader.readUnsignedByte();
            // Type — тип стрелочного перевода (Switch_...)

            int lengthM = reader.readUnsignedShort();
            // LenR — реальная длина стрелки, м

            String number = readString(reader, 5);
            // Name[5] — номер стрелки

            int stationCode = reader.readInt();
            // stCode — код принадлежности с битом StanDecor

            int passportFlag = reader.readUnsignedByte();
            // isPasp — 1 паспортные, 0 фактические данные

            if (passportFlag != 0 && passportFlag != 1) {
                throw new SrvParsingException(
                        "Invalid switch isPasp: " + passportFlag
                );
            }

            switches.add(new SrvSwitch(
                    stepIndex,
                    direction,
                    type,
                    lengthM,
                    number,
                    stationCode,
                    passportFlag == 1
            ));
        }

        return switches;
    }

    public List<SrvBuild> parseBuilds(
            SrvBinaryReader reader,
            long count
    ) {
        // CntBUILD — количество сооружений.
        checkCount(reader, count, BUILD_SIZE);

        List<SrvBuild> builds = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int startStepIndex = reader.readInt();
            // STP_BEG — индекс шага начала сооружения

            int endStepIndex = reader.readInt();
            // STP_END — индекс шага конца сооружения

            short type = reader.readShort();
            // Type — тип сооружения (BridgeTypeEnum)

            int passportFlag = reader.readUnsignedByte();
            // isPasp — источник данных о сооружении

            if (passportFlag != 0 && passportFlag != 1) {
                throw new SrvParsingException(
                        "Invalid build isPasp: " + passportFlag
                );
            }

            builds.add(new SrvBuild(
                    startStepIndex,
                    endStepIndex,
                    type,
                    passportFlag == 1
            ));
        }

        return builds;
    }


    public List<SrvSpeed> parseSpeeds(
            SrvBinaryReader reader,
            long count
    ) {
        // CntSPEED — количество участков установленных скоростей.
        checkCount(reader, count, SPEED_SIZE);

        List<SrvSpeed> speeds = new ArrayList<>();

        for (long i = 0; i < count; i++) {

            int startStepIndex = reader.readInt();
            // STP_BEG — индекс шага начала участка

            int endStepIndex = reader.readInt();
            // STP_END — индекс шага конца участка

            long packedStart = reader.readUnsignedInt();
            // Piket_BEG:4, KM_BEG:28 — упакованные координаты начала

            long packedEnd = reader.readUnsignedInt();
            // Piket_END:4, KM_END:28 — упакованные координаты конца

            int startPicket = (int) (packedStart & 0xF);
            int startKm = (int) (packedStart >>> 4);

            int endPicket = (int) (packedEnd & 0xF);
            int endKm = (int) (packedEnd >>> 4);

            int passengerSpeed = reader.readUnsignedShort();
            // VPass — максимальная скорость пассажирских поездов

            int freightSpeed = reader.readUnsignedShort();
            // VGruz — максимальная скорость грузовых поездов

            int emptyTrainSpeed = reader.readUnsignedShort();
            // VPorog — максимальная скорость порожних поездов

            speeds.add(new SrvSpeed(
                    startStepIndex,
                    endStepIndex,
                    startKm,
                    startPicket,
                    endKm,
                    endPicket,
                    passengerSpeed,
                    freightSpeed,
                    emptyTrainSpeed
            ));
        }

        return speeds;
    }


    public List<SrvThread> parseThreads(
            SrvBinaryReader reader,
            long count
    ) {
        // CntTHREAD — количество участков левой рихтовочной нити.
        checkCount(reader, count, THREAD_SIZE);

        List<SrvThread> threads = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int startStepIndex = reader.readInt();
            // STP_BEG — индекс шага начала участка

            int endStepIndex = reader.readInt();
            // STP_END — индекс шага конца участка

            threads.add(new SrvThread(
                    startStepIndex,
                    endStepIndex
            ));
        }

        return threads;
    }


    public List<SrvElevation> parseElevations(
            SrvBinaryReader reader,
            long count
    ) {
        // CntUROV — количество точек изменения возвышения.
        checkCount(reader, count, ELEVATION_SIZE);

        List<SrvElevation> elevations = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int stepIndex = reader.readInt();
            // STP — индекс шага точки изменения возвышения

            float valueMm = reader.readFloat();
            // Value — возвышение в миллиметрах

            elevations.add(new SrvElevation(
                    stepIndex,
                    valueMm
            ));
        }

        return elevations;
    }


    public List<SrvCurve> parseCurves(
            SrvBinaryReader reader,
            long count
    ) {
        // CntCURVE — количество точек параметров кривых.
        checkCount(reader, count, CURVE_SIZE);

        List<SrvCurve> curves = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int stepIndex = reader.readInt();
            // STP — индекс шага

            float radiusM = reader.readFloat();
            // RI — радиус кривой в метрах, 0 — прямая

            float gaugeNormMm = reader.readFloat();
            // SI — нормативная ширина колеи в мм

            float wearMm = reader.readFloat();
            // IZ — износ в мм

            float toleranceMm = reader.readFloat();
            // DP — допуск в мм

            curves.add(new SrvCurve(
                    stepIndex,
                    radiusM,
                    gaugeNormMm,
                    wearMm,
                    toleranceMm
            ));
        }

        return curves;
    }


    public List<SrvDefect> parseDefects(
            SrvBinaryReader reader,
            long count
    ) {
        // CntDEFECT — количество неисправностей из tsVPI_H.
        checkCount(reader, count, DEFECT_SIZE);

        List<SrvDefect> defects = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            int startStepIndex = reader.readInt();
            // STP_BEG — начальный шаг неисправности

            int endStepIndex = reader.readInt();
            // STP_END — конечный шаг неисправности

            int mask = reader.readUnsignedShort();
            // Mask — принадлежность: план, уровень, профиль и др.

            float valueMm = reader.readFloat();
            // Value — величина отступления в мм

            int typeCode = reader.readUnsignedShort();
            // Type — код неисправности, например 3116

            int passengerSpeed = reader.readUnsignedShort();
            // VPass — ограничение скорости пассажирских поездов

            int freightSpeed = reader.readUnsignedShort();
            // VGruz — ограничение скорости грузовых поездов

            int emptyTrainSpeed = reader.readUnsignedShort();
            // VPorog — ограничение скорости порожних поездов

            int degree = reader.readUnsignedByte();
            // Degree — степень отклонения

            String label = readString(reader, 7);
            // Label[7] — обозначение неисправности в Windows-1251

            defects.add(new SrvDefect(
                    startStepIndex,
                    endStepIndex,
                    mask,
                    valueMm,
                    typeCode,
                    passengerSpeed,
                    freightSpeed,
                    emptyTrainSpeed,
                    degree,
                    label
            ));
        }

        return defects;
    }


    public List<SrvGabarit> parseGabarits(
            SrvBinaryReader reader,
            long count
    ) {
        // CntGABARIT — количество записей о габаритах.
        checkCount(reader, count, GABARIT_SIZE);

        List<SrvGabarit> gabarits = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            double controlDistanceM = reader.readDouble();
            // KRD — контрольное расстояние от начала участка, м

            int stepIndex = reader.readInt();
            // STP — индекс шага

            // PKT — положение объекта, структура tsPKT_H.
            long packedPosition = reader.readUnsignedInt();
            // Piket:4, KM:24 — пикет и километр в одном числе

            int picket = (int) (packedPosition & 0xF);
            int kilometer = (int) ((packedPosition >>> 4) & 0xFFFFFF);

            float meter = reader.readFloat();
            // PKT.M — метры в пределах пикетажа

            // Type:8, Reserv:4, Clearance:2, Intertrack:2.
            int flags = reader.readUnsignedShort();

            int objectType = flags & 0xFF;           // Type — тип объекта
            int reservedFlags = (flags >>> 8) & 0xF; // Reserv — резерв
            int clearanceCode = (flags >>> 12) & 0x3; // Clearance — габарит
            int intertrackCode = (flags >>> 14) & 0x3; // Intertrack — междупутье

            int leftDistanceMm = reader.readUnsignedShort();
            // Distance[0] — расстояние до объекта слева, мм

            int rightDistanceMm = reader.readUnsignedShort();
            // Distance[1] — расстояние до объекта справа, мм

            short heightMm = reader.readShort();
            // Height — высота объекта, мм

            short leftRangeMm = reader.readShort();
            // Range[0] — допускаемое смещение пути влево, мм

            short rightRangeMm = reader.readShort();
            // Range[1] — допускаемое смещение пути вправо, мм

            String description = readString(reader, 64);
            // Label[64] — описание объекта, Windows-1251

            gabarits.add(new SrvGabarit(
                    controlDistanceM,
                    stepIndex,
                    kilometer,
                    picket,
                    meter,
                    objectType,
                    reservedFlags,
                    clearanceCode,
                    intertrackCode,
                    leftDistanceMm,
                    rightDistanceMm,
                    heightMm,
                    leftRangeMm,
                    rightRangeMm,
                    description
            ));
        }

        return gabarits;
    }

    public List<SrvGeoCoordinate> parseGeoCoordinates(
            SrvBinaryReader reader,
            long count
    ) {
        // CntGeoCoords — количество GNSS-точек на оси пути.
        checkCount(reader, count, GEO_COORD_SIZE);

        List<SrvGeoCoordinate> coordinates = new ArrayList<>();

        for (long i = 0; i < count; i++) {
            double latitude = reader.readDouble();
            // lat — географическая широта

            double longitude = reader.readDouble();
            // lon — географическая долгота

            double altitude = reader.readDouble();
            // alt — высота

            coordinates.add(new SrvGeoCoordinate(
                    latitude,
                    longitude,
                    altitude
            ));
        }

        return coordinates;
    }


}
