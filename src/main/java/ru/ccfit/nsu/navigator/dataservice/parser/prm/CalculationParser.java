package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.GeoCoordinateParser;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.GeoCoordinate;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCalculation;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmOtvod;

import java.util.List;
import java.util.Map;

public class CalculationParser {

	// Полный разбор двоичного файла проекта
	public PrmCalculation parse(byte[] fileData) {
		BinaryReader reader = new BinaryReader(fileData, PrmParsingException::new);

		PrmHeader header = new PrmHeaderParser().parse(reader);
		Map<Integer, byte[]> channelData = new PrmChannelDataParser().parse(reader, header);
		PrmCdtData cdtData = new PrmCdtParser().parse(reader, header);
		List<PrmOtvod> otvods = new PrmOtvodParser().parse(reader, header.otvodCount());
		List<GeoCoordinate> geoCoordinates = parseOptionalGeoCoordinates(reader);

		if (reader.remaining() != 0) {
			throw new PrmParsingException(
					"Unexpected trailing data at offset " + reader.position()
			);
		}

		return new PrmCalculation(
				header,
				channelData,
				cdtData,
				otvods,
				geoCoordinates
		);
	}

	// Разбор опциональной секции GNSS-координат после блока отводов
	private List<GeoCoordinate> parseOptionalGeoCoordinates(BinaryReader reader) {
		if (reader.remaining() < Integer.BYTES) {
			return List.of();
		}

		int geoCoordCount = reader.readInt();
		if (geoCoordCount < 0) {
			throw new PrmParsingException("Invalid geo coordinate count: " + geoCoordCount);
		}

		if (geoCoordCount == 0) {
			return List.of();
		}

		return new GeoCoordinateParser().parse(reader, geoCoordCount);
	}
}