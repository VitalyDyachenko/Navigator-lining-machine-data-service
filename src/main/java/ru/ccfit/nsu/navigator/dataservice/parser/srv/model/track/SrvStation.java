package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track;

public record SrvStation(
        int sourceId,
        int code,
        int departmentId,
        int okatoId,
        short typeId,
        String name
) {}