package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;

import java.util.ArrayList;
import java.util.List;

public class SrvMarkerParser {

    public List<SrvMarker> parse(
            SrvBinaryReader reader,
            int markerCount
    ) {
        // CntMARKER — количество маркеров из заголовка.
        List<SrvMarker> markers = new ArrayList<>(markerCount);

        for (int i = 0; i < markerCount; i++) { //todo По документации markerCount == 1?
            int type = reader.readUnsignedByte(); // Type — тип маркера
            long stepIndex = reader.readUnsignedInt(); // iStep — индекс шага

            markers.add(new SrvMarker(type, stepIndex));
        }

        return markers;
    }
}