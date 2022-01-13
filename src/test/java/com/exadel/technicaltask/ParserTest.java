package com.exadel.technicaltask;

import com.exadel.technicaltask.exception.parser.UnexpectedTokenParseException;
import com.exadel.technicaltask.model.ParseResult;
import com.exadel.technicaltask.parser.Parser;
import com.exadel.technicaltask.parser.ParseTokenGrammar;
import com.exadel.technicaltask.util.ParserUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParserTest {
    public static final String LOCAL_DELIMITER = ",";
    public static final String LOCAL_NODE_GROUP_START = "(";
    public static final String LOCAL_NODE_GROUP_END = ")";

    private String replaceLocalGrammarValuesWithGlobalValues(String text) {
        return text.replace(LOCAL_DELIMITER, ParseTokenGrammar.DELIMITER).replace(LOCAL_NODE_GROUP_START, ParseTokenGrammar.NODE_GROUP_START).replace(LOCAL_NODE_GROUP_END, ParseTokenGrammar.NODE_GROUP_END);
    }

    @SneakyThrows
    private void assertThatTextToFlatOutputIsEqualTo(String text, String expectedFlatOutput) {
        Parser parser = new Parser(text);

        String flatOutput = String.join("\n", ParserUtil.flatten(parser.parse(), (ParseResult parseResult, int depth) -> ("-".repeat(depth) + " " + parseResult.getValue()).trim()));

        assertThat(flatOutput).isEqualTo(expectedFlatOutput);
    }

    @SneakyThrows
    private void assertThatUnexpectedTokenParseExceptionWasThrownAtPosition(String text, Integer pos, String additionalDescription) {
        try {
            Parser parser = new Parser(this.replaceLocalGrammarValuesWithGlobalValues(text));

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
        this.assertThatTextToFlatOutputIsEqualTo("(id(test(test2(test3(test4)))),created,employee(id,firstname,employeeType(id),lastname),location(name2(name3(name5))))", "id\n- test\n-- test2\n--- test3\n---- test4\ncreated\nemployee\n- id\n- firstname\n- employeeType\n-- id\n- lastname\nlocation\n- name2\n-- name3\n--- name5");
        this.assertThatTextToFlatOutputIsEqualTo("(id,created,employee,lastname,loc,44)", "id\ncreated\nemployee\nlastname\nloc\n44");
        this.assertThatTextToFlatOutputIsEqualTo("(employee(jimmy, timmy, jones, alfred))", "employee\n- jimmy\n- timmy\n- jones\n- alfred");
        this.assertThatTextToFlatOutputIsEqualTo("(employee(jimmy, timmy, jones, alfred),carMake(bmw, mercedes))", "employee\n- jimmy\n- timmy\n- jones\n- alfred\ncarMake\n- bmw\n- mercedes");
        this.assertThatTextToFlatOutputIsEqualTo("(drawer(box1(sneakers, cap),box2(clothes)))", "drawer\n- box1\n-- sneakers\n-- cap\n- box2\n-- clothes");
    }

    @SneakyThrows
    @Test
    void testForInvalidInput() {
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("id,created,employee(id,firstname,employeeType(id),lastname),location)", 0, "missing open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,,created,employee(id,firstname,employeeType(id),lastname),location)", 4, "duplicate comma");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,created,employee((id,firstname,employeeType(id),lastname),location)", 21, "duplicate open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,created,employee(id,firstname,employeeType(id),lastname", 59, "unexpected end of text");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(id,created,employee(id(test)(hello),firstname,employeeType(id),lastname", 29, "unexpected open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(employee(jimmy, timmy, jones, alfred)))", 39, "unexpected open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(location, jim, tim, joe", 24, "unexpected end, expected open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(employee(location)))))))", 20, "unexpected open parenthesis");
        this.assertThatUnexpectedTokenParseExceptionWasThrownAtPosition("(employee(jimmy, timmy, jones, alfred),carMake(bmw, mercedes)),", 62, "unexpected ',' expected end");
    }
}
