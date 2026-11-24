package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.*;

import java.nio.charset.Charset;
import java.util.Arrays;

public class SrvTrackParser {

    private static final Charset CP1251 = Charset.forName("windows-1251");

    // tsPKT — позиция на пути: км, метры, миллиметры.
    private SrvTrackPosition parsePosition(SrvBinaryReader reader) {
        int km = reader.readInt();              // KM — километр
        short meters = reader.readShort();      // M — метры
        short millimeters = reader.readShort(); // MM — миллиметры

        return new SrvTrackPosition(km, meters, millimeters);
    }

    // tsDBN_DOR — железная дорога по классификатору.
    private SrvRailway parseRailway(SrvBinaryReader reader) {
        short code = reader.readShort();       // KOD — код дороги
        String name = readString(reader, 30);  // NAME[30] — название дороги
        String shortName = readString(reader, 6); // SNAME[6] — сокращение

        return new SrvRailway(code, name, shortName);
    }

    // tsDBN_UP — участок пути (направление).
    private SrvDirection parseDirection(SrvBinaryReader reader) {
        int sourceId = reader.readInt();       // ID — исходный ID направления
        short code = reader.readShort();       // KOD — код участка
        short magistralCode = reader.readShort(); // MAG_KOD — код магистрали
        String name = readString(reader, 50);  // NAME[50] — название участка
        int startStationId = reader.readInt(); // STAN1_ID — ID станции начала
        int endStationId = reader.readInt();   // STAN2_ID — ID станции конца

        return new SrvDirection(
                sourceId,
                code,
                magistralCode,
                name,
                startStationId,
                endStationId
        );
    }

    // Чтение строки фиксированной длины из C-структуры.
    private String readString(SrvBinaryReader reader, int length) {
        byte[] bytes = reader.readBytes(length);

        int end = 0;
        while (end < bytes.length && bytes[end] != 0) {
            end++;
        }

        return new String(
                Arrays.copyOf(bytes, end),
                CP1251
        ).trim();
    }

    public SrvTrack parse(SrvBinaryReader reader) {
        // NPUT[7] — номер пути, на котором проводилась съёмка.
        String trackNumber = readString(reader, 7);

        // PKT_BEGIN — начальная координата участка съёмки.
        SrvTrackPosition begin = parsePosition(reader);

        // PKT_END — конечная координата участка съёмки.
        SrvTrackPosition end = parsePosition(reader);

        // ADM_KOD — административный код государства.
        short administrativeCode = reader.readShort();

        // DOR — железная дорога по классификатору.
        SrvRailway railway = parseRailway(reader);

        // UP — участок пути (направление).
        SrvDirection direction = parseDirection(reader);

        // PUTGL — главный путь.
        SrvMainTrack mainTrack = parseMainTrack(reader);

        // PEREG_MS — межстанционный перегон.
        SrvPeregon peregon = parsePeregon(reader);

        // STAN1 — первая станция.
        SrvStation station1 = parseStation(reader);

        // STANKM1 — положение первой станции.
        SrvStationPosition stationPosition1 = parseStationPosition(reader);

        // STAN2 — вторая станция.
        SrvStation station2 = parseStation(reader);

        // STANKM2 — положение второй станции.
        SrvStationPosition stationPosition2 = parseStationPosition(reader);

        return new SrvTrack(
                trackNumber,
                begin,
                end,
                administrativeCode,
                railway,
                direction,
                mainTrack,
                peregon,
                station1,
                stationPosition1,
                station2,
                stationPosition2
        );
    }

    // tsDBN_PUTGL — главный путь из железнодорожного классификатора.
    private SrvMainTrack parseMainTrack(SrvBinaryReader reader) {
        int sourceId = reader.readInt(); // ID — исходный ID главного пути

        int directionSourceId = reader.readInt(); // UP_ID — ID направления

        String number = readString(reader, 7); // NOM[7] — номер главного пути

        short gaugeWidthId = reader.readShort(); // KOL_SHIR_ID — ID типа ширины колеи

        int mainTrackSourceIdOsn = reader.readInt(); // PUTGL_ID_OSN — ID основного пути

        return new SrvMainTrack(
                sourceId,
                directionSourceId,
                number,
                gaugeWidthId,
                mainTrackSourceIdOsn
        );
    }

    // tsDBN_PEREG_MS — межстанционный перегон.
    private SrvPeregon parsePeregon(SrvBinaryReader reader) {
        int sourceId = reader.readInt();              // ID — ID перегона
        int directionSourceId = reader.readInt();     // UP_ID — ID направления
        int startStationId = reader.readInt();        // STAN1_ID — ID начальной станции
        int endStationId = reader.readInt();          // STAN2_ID — ID конечной станции
        double exploitationLength = reader.readDouble(); // EXPL — эксплуатационная длина
        String name = readString(reader, 64);         // NAME[64] — название перегона

        return new SrvPeregon(
                sourceId,
                directionSourceId,
                startStationId,
                endStationId,
                exploitationLength,
                name
        );
    }

    // tsDBN_STAN — описание станции (раздельного пункта, не обязательно пасажир.станиция).
    private SrvStation parseStation(SrvBinaryReader reader) {
        int sourceId = reader.readInt();       // ID — исходный ID станции
        int code = reader.readInt();           // KOD — основной код станции
        int departmentId = reader.readInt();   // PRED_ID — ID отделения
        int okatoId = reader.readInt();        // OKATO_ID — ID ОКАТО
        short typeId = reader.readShort();     // TIP_ID — тип раздельного пункта
        String name = readString(reader, 38);  // NAME[38] — полное название

        return new SrvStation(
                sourceId,
                code,
                departmentId,
                okatoId,
                typeId,
                name
        );
    }


    // tsDBN_STANKM — ось и границы станции на главном пути.
    private SrvStationPosition parseStationPosition(
            SrvBinaryReader reader
    ) {
        int mainTrackSourceId = reader.readInt();  // PUTGL_ID — ID главного пути
        int stationSourceId = reader.readInt();    // STAN_ID — ID станции
        short axisAttributeId = reader.readShort(); // OS_PRIZ_ID — признак оси

        int axisKm = reader.readInt();    // KM — километр оси станции
        short axisM = reader.readShort(); // M — метры оси

        int startKm = reader.readInt();    // KMN — километр начала станции
        short startM = reader.readShort(); // MN — метры начала

        int endKm = reader.readInt();    // KMK — километр конца станции
        short endM = reader.readShort(); // MK — метры конца

        return new SrvStationPosition(
                mainTrackSourceId,
                stationSourceId,
                axisAttributeId,
                axisKm,
                axisM,
                startKm,
                startM,
                endKm,
                endM
        );
    }


}