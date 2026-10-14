package ru.ccfit.nsu.navigator.dataservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends AppException {
	public InvalidFileTypeException(String type) {
		super(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "Unknown file type: " + type + ". It can be only one of: ['survey', 'calculation', 'work']");
	}
}