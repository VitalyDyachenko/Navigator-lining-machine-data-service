package ru.ccfit.nsu.navigator.dataservice.parser.common.model.track;

import java.util.Objects;

public record TrackData(
		String trackNumber,
		TrackPosition begin,
		TrackPosition end,
		short administrativeCode,
		RailwayData railway,
		DirectionData direction,
		MainTrackData mainTrack,
		PeregonData peregon,
		StationData station1,
		StationPositionData stationPosition1,
		StationData station2,
		StationPositionData stationPosition2
) {
	// Сверка метаданных участка без учёта границ пикетажа
	public boolean hasSameMetadata(TrackData other) {
		if (other == null) {
			return false;
		}
		return administrativeCode == other.administrativeCode
				&& Objects.equals(trackNumber, other.trackNumber)
				&& Objects.equals(railway, other.railway)
				&& Objects.equals(direction, other.direction)
				&& Objects.equals(mainTrack, other.mainTrack)
				&& Objects.equals(peregon, other.peregon)
				&& Objects.equals(station1, other.station1)
				&& Objects.equals(stationPosition1, other.stationPosition1)
				&& Objects.equals(station2, other.station2)
				&& Objects.equals(stationPosition2, other.stationPosition2);
	}
}