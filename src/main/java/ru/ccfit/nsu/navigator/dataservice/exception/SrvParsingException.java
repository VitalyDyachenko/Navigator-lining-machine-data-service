package ru.ccfit.nsu.navigator.dataservice.exception;

public class SrvParsingException extends RuntimeException {
    public SrvParsingException(String message) {
        super(message);
    }
    public SrvParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
