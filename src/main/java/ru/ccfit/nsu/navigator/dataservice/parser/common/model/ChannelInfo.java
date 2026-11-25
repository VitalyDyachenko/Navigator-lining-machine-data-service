package ru.ccfit.nsu.navigator.dataservice.parser.common.model;

public record ChannelInfo(
        int valueType,
        int physicalType,
        float scale
) {}