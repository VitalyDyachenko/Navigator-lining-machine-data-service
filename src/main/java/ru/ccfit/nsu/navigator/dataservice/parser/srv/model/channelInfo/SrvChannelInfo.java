package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo;

public record SrvChannelInfo(
        int valueType,
        int physicalType,
        float scale
) {}