package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvHeader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class SrvChannelDataParser {

    private static final int CHANNEL_COUNT = 16;

    // tsDATA.Buff — массивы измерений типа short.
    private static final int BYTES_PER_VALUE = Short.BYTES;

    public Map<Integer, byte[]> parse(BinaryReader reader, SrvHeader header)
    {
        int mask = header.channelMask();    // MaskCH — маска записанных каналов
        long steps = header.stepCount();    // CntSTEP — число измерений на канал

        // Проверяем размер до выделения массива.
        long bytesPerChannel = steps * BYTES_PER_VALUE;

        if (bytesPerChannel > Integer.MAX_VALUE) { //TODO maybe to fix. readBytes need Integer.
            throw new SrvParsingException("Channel data is too large: " + bytesPerChannel);
        }

        Map<Integer, byte[]> channels = new HashMap<>();

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            // MaskCH — определяет наличие массива данных.
            boolean active = (mask & (1 << i)) != 0;
            if (!active) continue;

            // InfoCH[i] — описание канала.
            // Чтение данных от Val не зависит для подтверждённого формата. пока не используется.
            //SrvChannelInfo info = header.channels()[i];

            // Даже если Val = 0, сохраняем данные активного канала.
            // Несоответствие метаданных проверим отдельно.
            byte[] data = reader.readBytes((int) bytesPerChannel);

            channels.put(i, data);
        }

        return channels;
    }

    // Преобразует исходные байты Buff в знаковые 16-битные числа.
    public short[] decodeShortValues(byte[] data) {
        if (data.length % Short.BYTES != 0) {
            throw new SrvParsingException(
                    "Invalid channel data length: " + data.length
            );
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        short[] values = new short[data.length / Short.BYTES];

        for (int i = 0; i < values.length; i++) {
            values[i] = buffer.getShort();
        }

        return values;
    }
}