package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;
import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import java.util.ArrayList;
import java.util.List;

public class SrvMarkerParser {

    public List<SrvMarker> parse(
            SrvBinaryReader reader,
            int markerCount,
            long stepCount
    ) {
        List<SrvMarker> markers = new ArrayList<>(markerCount);

        for (int i = 0; i < markerCount; i++) {
            int type = reader.readUnsignedByte();
            long stepIndex = reader.readUnsignedInt();

            if (stepIndex > stepCount) {
                throw new SrvParsingException(
                        "Marker iStep " + stepIndex
                                + " exceeds CntSTEP " + stepCount
                );
            }

            markers.add(new SrvMarker(type, stepIndex));
        }

        return markers;
    }
}