package com.exadel.technicaltask.exception.parser;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;


@Getter(AccessLevel.PUBLIC)
public class UnexpectedTokenParseException extends ParseException {
    private final int pos;
    private final String receivedToken;
    private final List<String> possibleExpectedTokens;

    public UnexpectedTokenParseException(int pos, String receivedToken, List<String> possibleExpectedTokens) {
        super("unexpected token at position: '" + pos + "', received token: '" + receivedToken + "', expected token: " + String.join(" or ", possibleExpectedTokens));

        this.pos = pos;
        this.receivedToken = receivedToken;
        this.possibleExpectedTokens = possibleExpectedTokens;
    }
}
