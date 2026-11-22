package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SrvVpiDataParser {
    private static final Charset CP1251 =  Charset.forName("windows-1251");

    private static final int FRAGMENT_SIZE = 79;
    private static final int POST_SIZE = 9;

    // CntFRAG — количество фрагментов из tsVPI_H.

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
}
