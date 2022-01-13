package com.exadel.technicaltask.parser;

import com.exadel.technicaltask.exception.parser.InvalidParseInputException;
import com.exadel.technicaltask.exception.parser.UnexpectedTokenParseException;
import com.exadel.technicaltask.model.ParseResult;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final String text;

    private ParserSeekerStage prevSeekerStage;
    private ParserSeekerStage seekerStage;

    private Integer seekPosition = 0;

    private Integer nodeGroupDepth = 0;

    public Parser(@NonNull String text) {
        this.text = text.trim();
    }

    private ParserSeekerStage getSeekerStageByToken(String token) {
        switch (token) {
            case ParseTokenGrammar.DELIMITER:
                return ParserSeekerStage.REACHED_DELIMITER;
            case ParseTokenGrammar.NODE_GROUP_START:
                return ParserSeekerStage.REACHED_NODE_GROUP_START;
            case ParseTokenGrammar.NODE_GROUP_END:
                return ParserSeekerStage.REACHED_NODE_GROUP_END;
            // if we reached default case, then the token will be treated as part of the text value
            default:
                return ParserSeekerStage.LOOKING_FOR_TEXT;
        }
    }

    private void checkCurrentAndPrevSeekerStageForValidity(String receivedToken) throws UnexpectedTokenParseException {
        ParserSeekerStage currentStage = this.seekerStage;
        ParserSeekerStage prevStage = this.prevSeekerStage;

        if (Objects.equals(prevStage, ParserSeekerStage.STARTING) && !Objects.equals(currentStage, ParserSeekerStage.REACHED_NODE_GROUP_START)) {
            throw new UnexpectedTokenParseException(this.seekPosition, receivedToken, List.of(ParseTokenGrammar.NODE_GROUP_START));
        }

        if (Objects.equals(prevStage, ParserSeekerStage.REACHED_NODE_GROUP_START) && !Objects.equals(currentStage, ParserSeekerStage.LOOKING_FOR_TEXT)) {
            throw new UnexpectedTokenParseException(this.seekPosition, receivedToken, List.of("text"));
        }

        if (Objects.equals(prevStage, ParserSeekerStage.REACHED_DELIMITER) && !Objects.equals(currentStage, ParserSeekerStage.LOOKING_FOR_TEXT)) {
            throw new UnexpectedTokenParseException(this.seekPosition, receivedToken, List.of("text", ParseTokenGrammar.NODE_GROUP_END));
        }

        if (currentStage.equals(ParserSeekerStage.REACHED_END) && this.nodeGroupDepth > 0) {
            throw new UnexpectedTokenParseException(this.seekPosition, receivedToken, List.of(ParseTokenGrammar.NODE_GROUP_END));
        }

        if (this.nodeGroupDepth < 0 || (this.nodeGroupDepth == 0 && !List.of(ParserSeekerStage.REACHED_END, ParserSeekerStage.REACHED_NODE_GROUP_END).contains(currentStage))) {
            throw new UnexpectedTokenParseException(this.seekPosition, receivedToken, List.of("end"));
        }

        if (Objects.equals(prevStage, ParserSeekerStage.REACHED_NODE_GROUP_END) && !List.of(ParserSeekerStage.REACHED_DELIMITER, ParserSeekerStage.REACHED_NODE_GROUP_END, ParserSeekerStage.REACHED_END).contains(currentStage)) {
            throw new UnexpectedTokenParseException(this.seekPosition, receivedToken, List.of(ParseTokenGrammar.DELIMITER, ParseTokenGrammar.NODE_GROUP_END));
        }
    }

    private String seek() throws UnexpectedTokenParseException {
        this.prevSeekerStage = this.seekerStage;

        if (this.seekPosition >= this.text.length()) {
            this.seekerStage = ParserSeekerStage.REACHED_END;

            this.checkCurrentAndPrevSeekerStageForValidity(null);

            return null;
        }

        String token = String.valueOf(this.text.charAt(this.seekPosition));

        this.seekerStage = this.getSeekerStageByToken(token);

        if (this.seekerStage.equals(ParserSeekerStage.REACHED_NODE_GROUP_START)) {
            this.nodeGroupDepth++;
        }

        if (this.seekerStage.equals(ParserSeekerStage.REACHED_NODE_GROUP_END)) {
            this.nodeGroupDepth--;
        }

        this.checkCurrentAndPrevSeekerStageForValidity(token);

        this.seekPosition++;

        return token;
    }

    private void parseDown(ParseResult parseResult) throws UnexpectedTokenParseException {
        this.parseDown(parseResult, 0);
    }

    private void parseDown(ParseResult parseResult, int currentDepth) throws UnexpectedTokenParseException {
        List<ParseResult> children = new ArrayList<>();

        Integer textStartPosition = this.seekPosition;

        while (true) {
            this.seek();

            if (this.seekerStage.equals(ParserSeekerStage.REACHED_END)) {
                break;
            }

            if (this.prevSeekerStage.equals(this.seekerStage)) {
                if (this.prevSeekerStage.equals(ParserSeekerStage.REACHED_NODE_GROUP_END) && this.nodeGroupDepth != 0) {
                    break;
                }

                continue;
            }

            if (prevSeekerStage.equals(ParserSeekerStage.LOOKING_FOR_TEXT)) {
                String textValue = this.text.substring(textStartPosition, this.seekPosition - 1).trim();

                ParseResult childParseResult = new ParseResult();

                childParseResult.setValue(textValue);

                children.add(childParseResult);

                if (this.seekerStage.equals(ParserSeekerStage.REACHED_NODE_GROUP_END)) {
                    if (currentDepth == 0) {
                        continue;
                    } else {
                        break;
                    }
                }

                if (this.seekerStage.equals(ParserSeekerStage.REACHED_NODE_GROUP_START)) {
                    this.parseDown(childParseResult, currentDepth + 1);
                }
            }

            if (Arrays.asList(ParserSeekerStage.REACHED_DELIMITER, ParserSeekerStage.REACHED_NODE_GROUP_START).contains(this.seekerStage)) {
                textStartPosition = this.seekPosition;
            }
        }

        if (!children.isEmpty()) {
            parseResult.setChildren(children);
        }
    }

    private void resetProperties() {
        this.seekPosition = 0;
        this.prevSeekerStage = null;
        this.seekerStage = ParserSeekerStage.STARTING;
        this.nodeGroupDepth = 0;
    }

    public List<ParseResult> parse() throws InvalidParseInputException, UnexpectedTokenParseException {
        if (this.text.length() == 0) {
            throw new InvalidParseInputException();
        }

        this.resetProperties();

        ParseResult parseResult = new ParseResult();

        this.parseDown(parseResult);

        return parseResult.getChildren();
    }
}
