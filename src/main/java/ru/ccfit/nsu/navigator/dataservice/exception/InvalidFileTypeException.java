package ru.ccfit.nsu.navigator.dataservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends AppException {
	public InvalidFileTypeException(String type) {
		super(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "Unknown file type: " + type + ". It can be only one of: ['survey', 'calculation', 'work']");
	}

	private InvalidFileTypeException(HttpStatus status, String message) {
		super(status, "INVALID_FILE_TYPE", message);
	}

	// Создание исключения для пустого загруженного файла
	public static InvalidFileTypeException emptyFile(String partName, String expectedExtension) {
		return new InvalidFileTypeException(
				HttpStatus.BAD_REQUEST,
				"Empty file for '" + partName + "': expected " + expectedExtension + " file"
		);
	}

	// Создание исключения при несоответствии расширения файла
	public static InvalidFileTypeException invalidExtension(
			String partName,
			String expectedExtension,
			String filename
	) {
		return new InvalidFileTypeException(
				HttpStatus.BAD_REQUEST,
				"Invalid file extension for '" + partName + "': expected " + expectedExtension + ", got '" + filename + "'"
		);
	}

	// Создание исключения при обнаружении чужой известной сигнатуры
	public static InvalidFileTypeException unexpectedSignature(
			String partName,
			String expectedExtension,
			String expectedSignature,
			String actualSignature
	) {
		return new InvalidFileTypeException(
				HttpStatus.BAD_REQUEST,
				"Invalid file content for '" + partName + "': expected "
						+ expectedExtension + " (" + expectedSignature + "), got '" + actualSignature + "'"
		);
	}
}