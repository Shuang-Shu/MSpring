package com.mdc.mspring.mvc.exception;

public class JsonParseException extends RuntimeException {
    public JsonParseException() {
        super();
    }

    public JsonParseException(String message) {
        super(message);
    }
}
