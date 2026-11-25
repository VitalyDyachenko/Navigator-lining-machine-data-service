package ru.ccfit.nsu.navigator.dataservice.parser.common;

import ru.ccfit.nsu.navigator.dataservice.parser.common.model.track.*;

public class TrackParser {

	public TrackData parse(BinaryReader reader) {
		String trackNumber = reader.readFixedString(7);
		TrackPosition begin = parsePosition(reader);
		TrackPosition end = parsePosition(reader);
		short administrativeCode = reader.readShort();
		RailwayData railway = parseRailway(reader);
		DirectionData direction = parseDirection(reader);
		MainTrackData mainTrack = parseMainTrack(reader);
		PeregonData peregon = parsePeregon(reader);
		StationData station1 = parseStation(reader);
		StationPositionData stationPosition1 = parseStationPosition(reader);
		StationData station2 = parseStation(reader);
		StationPositionData stationPosition2 = parseStationPosition(reader);

		return new TrackData(
				trackNumber,
				begin,
				end,
				administrativeCode,
				railway,
				direction,
				mainTrack,
				peregon,
				station1,
				stationPosition1,
				station2,
				stationPosition2
		);
	}

	// Разбор структуры tsPKT
	private TrackPosition parsePosition(BinaryReader reader) {
		int km = reader.readInt();
		short meters = reader.readShort();
		short millimeters = reader.readShort();

		return new TrackPosition(km, meters, millimeters);
	}

	// Разбор структуры tsDBN_DOR
	private RailwayData parseRailway(BinaryReader reader) {
		short code = reader.readShort();
		String name = reader.readFixedString(30);
		String shortName = reader.readFixedString(6);

		return new RailwayData(code, name, shortName);
	}

	// Разбор структуры tsDBN_UP
	private DirectionData parseDirection(BinaryReader reader) {
		int sourceId = reader.readInt();
		short code = reader.readShort();
		short magistralCode = reader.readShort();
		String name = reader.readFixedString(50);
		int startStationId = reader.readInt();
		int endStationId = reader.readInt();

		return new DirectionData(
				sourceId,
				code,
				magistralCode,
				name,
				startStationId,
				endStationId
		);
	}

	// Разбор структуры tsDBN_PUTGL
	private MainTrackData parseMainTrack(BinaryReader reader) {
		int sourceId = reader.readInt();
		int directionSourceId = reader.readInt();
		String number = reader.readFixedString(7);
		short gaugeWidthId = reader.readShort();
		int mainTrackSourceIdOsn = reader.readInt();

		return new MainTrackData(
				sourceId,
				directionSourceId,
				number,
				gaugeWidthId,
				mainTrackSourceIdOsn
		);
	}

	// Разбор структуры tsDBN_PEREG_MS
	private PeregonData parsePeregon(BinaryReader reader) {
		int sourceId = reader.readInt();
		int directionSourceId = reader.readInt();
		int startStationId = reader.readInt();
		int endStationId = reader.readInt();
		double exploitationLength = reader.readDouble();
		String name = reader.readFixedString(64);

		return new PeregonData(
				sourceId,
				directionSourceId,
				startStationId,
				endStationId,
				exploitationLength,
				name
		);
	}

	// Разбор структуры tsDBN_STAN
	private StationData parseStation(BinaryReader reader) {
		int sourceId = reader.readInt();
		int code = reader.readInt();
		int departmentId = reader.readInt();
		int okatoId = reader.readInt();
		short typeId = reader.readShort();
		String name = reader.readFixedString(38);

		return new StationData(
				sourceId,
				code,
				departmentId,
				okatoId,
				typeId,
				name
		);
	}

	// Разбор структуры tsDBN_STANKM
	private StationPositionData parseStationPosition(BinaryReader reader) {
		int mainTrackSourceId = reader.readInt();
		int stationSourceId = reader.readInt();
		short axisAttributeId = reader.readShort();
		int axisKm = reader.readInt();
		short axisM = reader.readShort();
		int startKm = reader.readInt();
		short startM = reader.readShort();
		int endKm = reader.readInt();
		short endM = reader.readShort();

		return new StationPositionData(
				mainTrackSourceId,
				stationSourceId,
				axisAttributeId,
				axisKm,
				axisM,
				startKm,
				startM,
				endKm,
				endM
		);
	}
}