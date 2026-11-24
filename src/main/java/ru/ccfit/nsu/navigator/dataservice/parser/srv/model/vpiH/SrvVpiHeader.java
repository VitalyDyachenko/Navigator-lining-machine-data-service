package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiH;

public record SrvVpiHeader(
        SrvTripId tripId,
        long startStep,
        long fragmentCount,
        long postCount,
        long switchCount,
        long buildCount,
        long speedCount,
        long threadCount,
        long elevationCount,
        long curveCount,
        long defectCount,
        long gabaritCount,
        long geoCoordCount,
        long[] reserved
) {}
