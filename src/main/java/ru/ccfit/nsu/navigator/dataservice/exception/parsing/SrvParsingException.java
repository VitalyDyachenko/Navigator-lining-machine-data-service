package ru.ccfit.nsu.navigator.dataservice.exception.parsing;

public class SrvParsingException extends ParserException {
    public SrvParsingException(String message) {
        super("SRV file parsing error: " + message);
    }
}
