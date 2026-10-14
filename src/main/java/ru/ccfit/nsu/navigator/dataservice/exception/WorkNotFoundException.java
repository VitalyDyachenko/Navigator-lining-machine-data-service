package ru.ccfit.nsu.navigator.dataservice.exception;

import org.springframework.http.HttpStatus;

public class WorkNotFoundException extends AppException {
	public WorkNotFoundException(Long id) {
		super(HttpStatus.NOT_FOUND, "WORK_NOT_FOUND", "Work with id " + id + " not found");
	}
}
