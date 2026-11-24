package ru.ccfit.nsu.navigator.dataservice.exception.parsing;

import org.springframework.http.HttpStatus;
import ru.ccfit.nsu.navigator.dataservice.exception.AppException;

public class ParserException extends AppException {
	public ParserException() {
		super(HttpStatus.BAD_REQUEST, "PARSER_EXCEPTION", "Parsing validation error");
	}
	public ParserException(String message) {
		super(HttpStatus.BAD_REQUEST, "PARSER_EXCEPTION", message);
	}
}
