package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.common.TimeParser;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmOtvod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrmOtvodParser {

	private static final int OTVOD_SIZE = 150;
	private static final int POINTS_PER_PART = 4;
	private static final int INITIAL_Y_PER_PART = 2;

	// Разбор массива отводов tsOTVOD
	public List<PrmOtvod> parse(BinaryReader reader, int otvodCount) {
		reader.requireRecordCount(otvodCount, OTVOD_SIZE, "otvod");

		TimeParser timeParser = new TimeParser();
		List<PrmOtvod> otvods = new ArrayList<>(otvodCount);

		for (int i = 0; i < otvodCount; i++) {
			int rawFlags = reader.readUnsignedShort();
			int typeMask = rawFlags & 0x1F;
			int geometryMask = (rawFlags >>> 5) & 0x7;
			int mode = (rawFlags >>> 8) & 0xF;
			int source = (rawFlags >>> 12) & 0x7;
			boolean autoByClearance = (rawFlags & 0x8000) != 0;

			double[] startPointsM = readDoubleArray(reader, POINTS_PER_PART);
			double[] endPointsM = readDoubleArray(reader, POINTS_PER_PART);
			double[] startCoefficients = readDoubleArray(reader, POINTS_PER_PART);
			double[] endCoefficients = readDoubleArray(reader, POINTS_PER_PART);
			short[] startInitialYTenthMm = readShortArray(reader, INITIAL_Y_PER_PART);
			short[] endInitialYTenthMm = readShortArray(reader, INITIAL_Y_PER_PART);

			short deltaPlaneLeft = reader.readShort();
			short deltaPlaneRight = reader.readShort();
			short deltaProfileLeft = reader.readShort();
			short deltaProfileRight = reader.readShort();

			LocalDateTime creationTime = timeParser.parseOptional(reader);

			otvods.add(new PrmOtvod(
					rawFlags,
					typeMask,
					(typeMask & 0x01) != 0,
					(typeMask & 0x02) != 0,
					(typeMask & 0x04) != 0,
					(typeMask & 0x08) != 0,
					(typeMask & 0x10) != 0,
					geometryMask,
					mode,
					source,
					autoByClearance,
					startPointsM,
					endPointsM,
					startCoefficients,
					endCoefficients,
					startInitialYTenthMm,
					endInitialYTenthMm,
					deltaPlaneLeft,
					deltaPlaneRight,
					deltaProfileLeft,
					deltaProfileRight,
					creationTime
			));
		}

		return otvods;
	}

	private double[] readDoubleArray(BinaryReader reader, int length) {
		double[] values = new double[length];
		for (int i = 0; i < length; i++) {
			values[i] = reader.readDouble();
		}
		return values;
	}

	private short[] readShortArray(BinaryReader reader, int length) {
		short[] values = new short[length];
		for (int i = 0; i < length; i++) {
			values[i] = reader.readShort();
		}
		return values;
	}
}