package ru.ccfit.nsu.navigator.dataservice.parser.srv;


import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;

import java.util.ArrayList;
import java.util.List;

public class SrvRfidParser {

    public List<SrvRfid> parse(
            BinaryReader reader,
            int rfidCount
    ) {
        // CntRFID — количество RFID-меток из заголовка.
        List<SrvRfid> rfids = new ArrayList<>(rfidCount);

        for (int i = 0; i < rfidCount; i++) {
            // TS — упакованные пикет, километр и тип маркера.
            long ts = reader.readUnsignedInt();

            int picket = (int) (ts & 0xF);              // Piket:4
            int kilometer = (int) ((ts >>> 4) & 0xFFFFFF); // KM:24
            int markerType = (int) ((ts >>> 28) & 0xF); // Type:4

            // STP — счётчик импульсов датчика пути.
            long syncPulseCount = reader.readUnsignedInt();

            // ID — идентификатор RFID-метки.
            long tagIdentifier = reader.readUnsignedInt();

            // BLOCK[3] — три блока данных метки.
            long[] dataBlocks = new long[3];

            for (int j = 0; j < dataBlocks.length; j++) {
                dataBlocks[j] = reader.readUnsignedInt();
            }

            // State — упакованные состояние блоков и признаки ошибки.
            int state = reader.readUnsignedShort();

            int blockMask = state & 0xF;          // MskBLOCK:4
            int pointCount = (state >>> 4) & 0x7FF; // CntPOINT:11
            boolean hasError = (state & 0x8000) != 0; // Error:1

            // KRD — контрольная длина от начала участка, метры.
            double controlDistanceM = reader.readDouble();

            rfids.add(new SrvRfid(
                    kilometer,
                    picket,
                    markerType,
                    syncPulseCount,
                    tagIdentifier,
                    dataBlocks,
                    blockMask,
                    pointCount,
                    hasError,
                    controlDistanceM
            ));
        }

        return rfids;
    }
}