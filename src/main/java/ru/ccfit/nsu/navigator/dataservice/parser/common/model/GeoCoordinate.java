package ru.ccfit.nsu.navigator.dataservice.parser.common.model;

public record GeoCoordinate(
        double latitude,
        double longitude,
        double altitude
) {}