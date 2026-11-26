package ru.ccfit.nsu.navigator.dataservice.parser.common;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class TimeParser {

	// Разбор обязательной структуры tsHTIME
	public LocalDateTime parse(BinaryReader reader) {
		int offset = reader.position();
		long packedTime = reader.readUnsignedInt();
		return decode(reader, offset, packedTime);
	}

	// Разбор опциональной структуры tsHTIME с возвратом null при нулевом значении
	public LocalDateTime parseOptional(BinaryReader reader) {
		int offset = reader.position();
		long packedTime = reader.readUnsignedInt();

		if (packedTime == 0L) {
			return null;
		}

		return decode(reader, offset, packedTime);
	}

	// Безопасное декодирование упакованного значения tsHTIME с возвратом null при ошибке календаря
	public LocalDateTime tryDecode(long packedTime) {
		if (packedTime == 0L) {
			return null;
		}

		int year = (int) (packedTime & 0xFFF);
		int month = (int) ((packedTime >>> 12) & 0xF);
		int day = (int) ((packedTime >>> 16) & 0x1F);
		int hours = (int) ((packedTime >>> 21) & 0x1F);
		int minutes = (int) ((packedTime >>> 26) & 0x3F);

		try {
			return LocalDateTime.of(year, month, day, hours, minutes);
		} catch (DateTimeException e) {
			return null;
		}
	}

	private LocalDateTime decode(BinaryReader reader, int offset, long packedTime) {
		LocalDateTime decoded = tryDecode(packedTime);
		if (decoded == null) {
			throw reader.fail("Invalid date-time at offset " + offset);
		}
		return decoded;
	}
}