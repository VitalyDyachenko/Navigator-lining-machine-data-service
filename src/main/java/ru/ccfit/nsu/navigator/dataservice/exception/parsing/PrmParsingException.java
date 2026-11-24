package ru.ccfit.nsu.navigator.dataservice.exception.parsing;

public class PrmParsingException extends ParserException {
	public PrmParsingException(String message) {
		super("PRM file parsing error: " + message);
	}
}
