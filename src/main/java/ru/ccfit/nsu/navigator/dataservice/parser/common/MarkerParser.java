package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.marker.MarkerData;

import java.util.ArrayList;
import java.util.List;

public class MarkerParser {

    private static final int MARKER_SIZE = 5;

    // Разбор массива маркеров tsMARKER без ограничения по шагам
    public List<MarkerData> parse(BinaryReader reader, int markerCount) {
        reader.requireRecordCount(markerCount, MARKER_SIZE, "marker");

        List<MarkerData> markers = new ArrayList<>(markerCount);

        for (int i = 0; i < markerCount; i++) {
            int type = reader.readUnsignedByte();
            long stepIndex = reader.readUnsignedInt();
            markers.add(new MarkerData(type, stepIndex));
        }

        return markers;
    }

    // Разбор массива маркеров tsMARKER с проверкой верхней границы индекса шага
    public List<MarkerData> parse(
            BinaryReader reader,
            int markerCount,
            long stepCount
    ) {
        List<MarkerData> markers = parse(reader, markerCount);

        for (MarkerData marker : markers) {
            if (marker.stepIndex() > stepCount) {
                throw reader.fail(
                        "Marker iStep " + marker.stepIndex()
                                + " exceeds CntSTEP " + stepCount
                );
            }
        }

        return markers;
    }
}