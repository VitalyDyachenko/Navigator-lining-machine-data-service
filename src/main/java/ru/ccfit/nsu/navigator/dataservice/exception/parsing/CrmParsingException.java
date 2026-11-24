package ru.ccfit.nsu.navigator.dataservice.exception.parsing;

public class CrmParsingException extends ParserException {
	public CrmParsingException(String message) {
		super("CRM file parsing error: " + message);
	}
}
