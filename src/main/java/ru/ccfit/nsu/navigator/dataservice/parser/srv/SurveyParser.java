
package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH.SrvVpiHeader;

import java.util.List;
import java.util.Map;

public class SurveyParser {

    public SrvSurvey parse(byte[] fileData) {
        SrvBinaryReader reader = new SrvBinaryReader(fileData);

        // tsHEADER_SURVEY — заголовок съёмки.
        SrvHeader header = new SrvHeaderParser().parse(reader);

        // tsVPI_H — счётчики объектов ВПИ.
        SrvVpiHeader vpiHeader = header.vpi();

        // tsDATA.Buff — данные каналов по MaskCH и CntSTEP.
        Map<Integer, byte[]> channelData =
                new SrvChannelDataParser().parse(reader, header);

        // tsMARKER — CntMARKER маркеров съёмки.
        List<SrvMarker> markers = new SrvMarkerParser().parse(
                reader,
                header.markerCount(),
                header.stepCount()
        );
        // tsRFID — CntRFID меток.
        List<SrvRfid> rfids = new SrvRfidParser().parse(
                reader,
                header.rfidCount()
        );

        // tsVPI_D — внешние данные вагона-путеизмерителя.
        SrvVpiDataParser vpiParser = new SrvVpiDataParser();

        SrvVpiData vpiData = new SrvVpiData(
                vpiParser.parseFragment(reader, vpiHeader.fragmentCount()),       // Frag
                vpiParser.parsePosts(reader, vpiHeader.postCount()),             // Post
                vpiParser.parseSwitches(reader, vpiHeader.switchCount()),        // Switch
                vpiParser.parseBuilds(reader, vpiHeader.buildCount()),           // Build
                vpiParser.parseSpeeds(reader, vpiHeader.speedCount()),           // Speed
                vpiParser.parseThreads(reader, vpiHeader.threadCount()),         // Thread
                vpiParser.parseElevations(reader, vpiHeader.elevationCount()),    // Urov
                vpiParser.parseCurves(reader, vpiHeader.curveCount()),           // Curve
                vpiParser.parseDefects(reader, vpiHeader.defectCount()),         // Defect
                vpiParser.parseGabarits(reader, vpiHeader.gabaritCount()),       // Gabarit
                vpiParser.parseGeoCoordinates(reader, vpiHeader.geoCoordCount()) // GeoCoords
        );

        if (reader.remaining() != 0) {
            throw new SrvParsingException("Unexpected trailing data at offset " + reader.position());
        }

        return new SrvSurvey(
                header,
                channelData,
                markers,
                rfids,
                vpiData
        );
    }
}
