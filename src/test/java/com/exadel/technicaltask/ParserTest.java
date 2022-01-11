package com.exadel.technicaltask;

import com.exadel.technicaltask.exception.parser.UnexpectedTokenParseException;
import com.exadel.technicaltask.model.ParseResult;
import com.exadel.technicaltask.parser.Parser;
import com.exadel.technicaltask.util.ParserUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParserTest {

    @SneakyThrows
    private void assertThatTextToFlatOutputIsEqualTo(String text, String expectedFlatOutput) {
        Parser parser = new Parser(text);

        String flatOutput = ParserUtil.flatten(parser.parse(), (ParseResult parseResult, int depth) -> ("-".repeat(depth) + " " + parseResult.getValue()).trim()).stream().map(Object::toString).collect(Collectors.joining("\n"));

        assertThat(flatOutput).isEqualTo(expectedFlatOutput);
    }

    @SneakyThrows
    private void assertThatUnexpectedTokenParseExceptionWasThrownAtPosition(String text, Integer pos, String additionalDescription) {
        try {
            Parser parser = new Parser(text);

            parser.parse();

            if (additionalDescription != null) {
                additionalDescription = "(" + additionalDescription + ")";
            }

            throw new Exception("UnexpectedTokenParseException should have been thrown at position " + pos + " " + additionalDescription);
        } catch (UnexpectedTokenParseException exception) {
            assertThat(exception.getPos()).isEqualTo(pos);
        }
    }

    @SneakyThrows
    private void assertThatUnexpectedTokenParseExceptionWasThrownAtPosition(String text, Integer pos) {
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition(text, pos, null);
    }

    @SneakyThrows
    @Test
    void testForValidInput() {
        this.assertThatTextToFlatOutputIsEqualTo("(id,created,employee(id,firstname,employeeType(id),lastname),location)", "id\ncreated\nemployee\n- id\n- firstname\n- employeeType\n-- id\n- lastname\nlocation");
        this.assertThatTextToFlatOutputIsEqualTo("(id(test(test2(test3(test4)))),created,employee(id,firstname,employeeType(id),lastname),location)", "id\n- test\n-- test2\n--- test3\n---- test4\ncreated\nemployee\n- id\n- firstname\n- employeeType\n-- id\n- lastname\nlocation");
    }

    @SneakyThrows
    @Test
    void testForInvalidInput() {
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("id,created,employee(id,firstname,employeeType(id),lastname),location)", 0, "missing open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,,created,employee(id,firstname,employeeType(id),lastname),location)", 4, "duplicate comma");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,created,employee((id,firstname,employeeType(id),lastname),location)", 21, "duplicate open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,created,employee(id,firstname,employeeType(id),lastname", 59, "unexpected end of text");
    }
}
