package com.exadel.technicaltask.exception.parser;

public class InvalidParseInputException extends ParseException {
    public InvalidParseInputException(String message) {
        super(message);
    }

    public InvalidParseInputException() {
        super("invalid text passed as input");
    }
}
