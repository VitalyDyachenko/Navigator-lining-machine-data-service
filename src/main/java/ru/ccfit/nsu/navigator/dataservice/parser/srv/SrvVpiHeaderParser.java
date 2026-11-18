
package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvTripId;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

public class SrvVpiHeaderParser {

    public SrvVpiHeader parse(SrvBinaryReader reader) {
        // ID_Trip — идентификатор проезда.
        SrvTripId tripId = parseTripId(reader);

        // StartSTP — начальный шаг относительно начала проезда.
        long startStep = reader.readUnsignedInt();

        // CntXXX — количество записей соответствующего типа.
        long fragmentCount = reader.readUnsignedInt(); // CntFRAG
        long postCount = reader.readUnsignedInt();     // CntPOST
        long switchCount = reader.readUnsignedInt();   // CntSWITCH
        long buildCount = reader.readUnsignedInt();    // CntBUILD
        long speedCount = reader.readUnsignedInt();    // CntSPEED
        long threadCount = reader.readUnsignedInt();   // CntTHREAD
        long elevationCount = reader.readUnsignedInt();// CntUROV
        long curveCount = reader.readUnsignedInt();    // CntCURVE
        long defectCount = reader.readUnsignedInt();   // CntDEFECT
        long gabaritCount = reader.readUnsignedInt();  // CntGABARIT
        long geoCoordCount = reader.readUnsignedInt(); // CntGeoCoords

        // Reserv[5] — пять зарезервированных unsigned int.
        long[] reserved = new long[5];
        for (int i = 0; i < reserved.length; i++) {
            reserved[i] = reader.readUnsignedInt();
        }

        return new SrvVpiHeader(
                tripId, startStep,
                fragmentCount, postCount, switchCount,
                buildCount, speedCount, threadCount,
                elevationCount, curveCount, defectCount,
                gabaritCount, geoCoordCount, reserved
        );
    }

    // tsID_TRIP — 64-битный идентификатор проезда.
    private SrvTripId parseTripId(SrvBinaryReader reader) {
        long raw = reader.readLong();

        return new SrvTripId(
                (int) (raw & 0xFF),
                (int) ((raw >>> 8) & 0xFFFF),
                (int) ((raw >>> 24) & 0x3FF),
                (int) ((raw >>> 34) & 0xF),
                (int) ((raw >>> 38) & 0xF),
                (int) ((raw >>> 42) & 0x3FFF),
                ((raw >>> 56) & 1) != 0,
                (int) ((raw >>> 57) & 0x7F)
        );
    }
}
