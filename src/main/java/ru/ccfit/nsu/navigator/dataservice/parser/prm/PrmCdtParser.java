package ru.ccfit.nsu.navigator.dataservice.parser.prm;

import ru.ccfit.nsu.navigator.dataservice.exception.parsing.PrmParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.BinaryReader;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtData;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmCdtPoint;
import ru.ccfit.nsu.navigator.dataservice.parser.prm.model.PrmHeader;

import java.util.ArrayList;
import java.util.List;

public class PrmCdtParser {

	private static final int MASK_TRACK_SRV = 0x4;
	private static final int MASK_TRACK_PRJ = 0x8;
	private static final int POINT_3D_SIZE = 24;
	private static final int EXT_D_SIZE = 34;

	// Разбор секции траекторий CDT в зависимости от версии файла
	public PrmCdtData parse(BinaryReader reader, PrmHeader header) {
		int pointsMask = header.cdtHeader().pointsMask();
		long stepCount = header.stepCount();

		if (stepCount > Integer.MAX_VALUE) {
			throw new PrmParsingException("Step count is too large for CDT: " + stepCount);
		}

		if (header.version() >= PrmHeaderParser.VERSION_PRJ_M) {
			List<PrmCdtPoint> measuredAxis = List.of();
			List<PrmCdtPoint> projectAxis = List.of();

			if ((pointsMask & MASK_TRACK_SRV) != 0) {
				measuredAxis = parsePoint3DList(reader, (int) stepCount, "measured CDT point");
			}

			if ((pointsMask & MASK_TRACK_PRJ) != 0) {
				projectAxis = parsePoint3DList(reader, (int) stepCount, "project CDT point");
			}

			return new PrmCdtData(measuredAxis, projectAxis);
		}

		if (pointsMask == 0) {
			return new PrmCdtData(List.of(), List.of());
		}

		List<PrmCdtPoint> legacyProjectAxis = parseLegacyExtDList(reader, (int) stepCount);
		return new PrmCdtData(List.of(), legacyProjectAxis);
	}

	private List<PrmCdtPoint> parsePoint3DList(BinaryReader reader, int count, String recordName) {
		reader.requireRecordCount(count, POINT_3D_SIZE, recordName);
		List<PrmCdtPoint> points = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			double x = reader.readDouble();
			double y = reader.readDouble();
			double h = reader.readDouble();
			points.add(new PrmCdtPoint(x, y, h));
		}

		return points;
	}

	// Разбор устаревшего формата tsEXT_D с преобразованием в координаты проектной оси
	private List<PrmCdtPoint> parseLegacyExtDList(BinaryReader reader, int count) {
		reader.requireRecordCount(count, EXT_D_SIZE, "legacy CDT point");
		List<PrmCdtPoint> points = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			reader.readDouble();
			double planeX = reader.readDouble();
			double planeY = reader.readDouble();
			double heightGr = reader.readDouble();
			reader.readShort();
			points.add(new PrmCdtPoint(planeX, planeY, heightGr));
		}

		return points;
	}
}