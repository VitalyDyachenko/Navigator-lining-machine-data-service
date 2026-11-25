package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.GeoCoordinate;

import java.util.ArrayList;
import java.util.List;

public class GeoCoordinateParser {

	private static final int GEO_COORD_SIZE = 24;

	// Разбор массива GNSS-координат tsGEO_COORD
	public List<GeoCoordinate> parse(BinaryReader reader, long count) {
		reader.requireRecordCount(count, GEO_COORD_SIZE, "geo coordinate");

		List<GeoCoordinate> coordinates = new ArrayList<>();

		for (long i = 0; i < count; i++) {
			double latitude = reader.readDouble();
			double longitude = reader.readDouble();
			double altitude = reader.readDouble();

			coordinates.add(new GeoCoordinate(latitude, longitude, altitude));
		}

		return coordinates;
	}
}