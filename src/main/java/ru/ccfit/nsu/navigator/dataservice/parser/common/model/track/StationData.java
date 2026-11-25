package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

public record StationData(
        int sourceId,
        int code,
        int departmentId,
        int okatoId,
        short typeId,
        String name
) {}