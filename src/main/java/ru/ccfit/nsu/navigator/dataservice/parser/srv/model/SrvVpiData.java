package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

import java.util.List;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.GeoCoordinate;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvDefect;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.*;

public record SrvVpiData(
        SrvFragment fragment,
        List<SrvPost> posts,
        List<SrvSwitch> switches,
        List<SrvBuild> builds,
        List<SrvSpeed> speeds,
        List<SrvThread> threads,
        List<SrvElevation> elevations,
        List<SrvCurve> curves,
        List<SrvDefect> defects,
        List<SrvGabarit> gabarits,
        List<GeoCoordinate> geoCoordinates
) {}
