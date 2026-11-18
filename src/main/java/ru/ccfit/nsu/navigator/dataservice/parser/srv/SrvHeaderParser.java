package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeaderPrefix;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvMachine;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvSurveyFlags;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;

public class SrvHeaderParser {
    public SrvHeaderPrefix parse(SrvBinaryReader reader){
        String signature = new String(reader.readBytes(6), StandardCharsets.US_ASCII);
        if (!signature.equals("NavSRV")){
            throw new SrvParsingException(
                    "Invalid SRV file signature: " + signature
            );
        }

        int version = reader.readUnsignedShort();

        LocalDateTime surveyTime = parseTime(reader);
        SrvMachine machine = parseMachine(reader);

        SrvSurveyFlags flags = parseFlags(reader);

        int channelMask = reader.readUnsignedShort();
        int stepSizeMm = reader.readUnsignedShort();
        long stepCount = reader.readUnsignedInt();
        int markerCount = reader.readUnsignedShort();
        int rfidCount = reader.readUnsignedShort();

        return new SrvHeaderPrefix(
                signature,
                version,
                surveyTime,
                machine,
                flags,
                channelMask,
                stepSizeMm,
                stepCount,
                markerCount,
                rfidCount
        );
    }


    private LocalDateTime parseTime(SrvBinaryReader reader) {
        long packedTime = reader.readUnsignedInt();

        int year = (int) (packedTime & 0xFFF);
        int month = (int) ((packedTime >>> 12) & 0xF);
        int day = (int) ((packedTime >>> 16) & 0x1F);
        int hours = (int) ((packedTime >>> 21) & 0x1F);
        int minutes = (int) ((packedTime >>> 26) & 0x3F);

        try {
            return LocalDateTime.of(year, month, day, hours, minutes);
        } catch (DateTimeException e) {
            throw new SrvParsingException(
                    "Invalid survey date at offset 8", e
            );
        }
    }

    private SrvMachine parseMachine(SrvBinaryReader reader) {

        int rawType = reader.readUnsignedByte();
        parseMachineType(rawType);

        byte[] nameBytes = reader.readBytes(24);

        String name = new String(nameBytes,
                Charset.forName("windows-1251")).split("\0", 2)[0].trim();

        long number = reader.readUnsignedInt();

        int[] lr = new int[5]; //Геометрия рихтовочной системы
        for (int i = 0; i < lr.length; i++) {
            lr[i] = reader.readUnsignedShort();
        }

        int[] ln = new int[5];  //Геометрия нивелировочной системы
        for (int i = 0; i < ln.length; i++) {
            ln[i] = reader.readUnsignedShort();
        }

        int[] t = new int[4]; //геометрия тележек мм
        for (int i = 0; i < t.length; i++) {
            t[i] = reader.readUnsignedShort();
        }

        int gaugeWidth = reader.readUnsignedShort(); //Ширина колеи

        return new SrvMachine(
                rawType,
                (rawType >>> 5) & 0b111,
                name,
                number,
                ((rawType >>> 4) & 1) != 0,
                ((rawType >>> 3) & 1) != 0,
                ((rawType >>> 2) & 1) != 0,
                rawType & 0b11,
                lr,
                ln,
                t,
                gaugeWidth
        );
    }

    private void parseMachineType(int rawType) {
        int machineType = (rawType >>> 5) & 0b111;

        boolean continuousCorrection = ((rawType >>> 4) & 1) != 0; //Непрерывная выправка

        boolean profileEnabled = ((rawType >>> 3) & 1) != 0; //Выправка по профилю

        boolean planeSatellite = ((rawType >>> 2) & 1) != 0; //Сателлит в плане

        int tampingSleepers = rawType & 0b11; //Подбиваемых шпал

        System.out.println("Machine type: " + machineType);
        System.out.println("Continuous correction: " + continuousCorrection);
        System.out.println("Profile enabled: " + profileEnabled);
        System.out.println("Plane satellite: " + planeSatellite);
        System.out.println("Tamping sleepers: " + tampingSleepers);
    }
    private SrvSurveyFlags parseFlags(SrvBinaryReader reader) {
        int raw = reader.readUnsignedShort();

        return new SrvSurveyFlags(
                (raw & 1) != 0,
                ((raw >>> 1) & 1) != 0,
                (raw >>> 2) & 0b11,
                ((raw >>> 4) & 1) != 0,
                (raw >>> 5) & 0b11,
                ((raw >>> 7) & 1) != 0,
                ((raw >>> 8) & 1) != 0,
                (raw >>> 9) & 0x7F
        );
    }
}

