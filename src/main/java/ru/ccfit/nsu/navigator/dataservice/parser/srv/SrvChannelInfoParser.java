package ru.ccfit.nsu.navigator.dataservice.parser.srv;

import ru.ccfit.nsu.navigator.dataservice.parser.SrvBinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo.SrvChannelInfo;

public class SrvChannelInfoParser {

    private static final int CHANNEL_COUNT = 16; // BUFF_MAX_CHANNEL — максимальное число каналов

    public SrvChannelInfo[] parse(SrvBinaryReader reader) {
        SrvChannelInfo[] channels = new SrvChannelInfo[CHANNEL_COUNT];

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            int valueType = reader.readUnsignedByte();  // Val — тип хранения данных (CH_VAL_xxx)
            int physicalType = reader.readUnsignedByte(); // Type — тип физической величины (CH_TYPE_xxx)
            float scale = reader.readFloat();           // Scale — делитель значений Data / Scale

            channels[i] = new SrvChannelInfo(
                    valueType,
                    physicalType,
                    scale
            );
        }

        return channels;
    }
}