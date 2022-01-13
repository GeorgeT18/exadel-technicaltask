package com.exadel.technicaltask.controller;

import com.exadel.technicaltask.exception.parser.ParseException;
import com.exadel.technicaltask.model.ParseResult;
import com.exadel.technicaltask.parser.Parser;
import com.exadel.technicaltask.util.ParserUtil;
import com.exadel.technicaltask.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/")
public class MainController {
    private static final String SESSION_ERROR_MESSAGE_PROPERTY_NAME = "__errorMessage";

    @Autowired
    private SessionUtil sessionUtil;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("defaultTextToBeParsed", "(id,created,employee(id,firstname,employeeType(id),lastname),location)");

        String errorMessage = sessionUtil.get(SESSION_ERROR_MESSAGE_PROPERTY_NAME);

        model.addAttribute("errorMessage", errorMessage);

        return "home";
    }

    @PostMapping
    public String submit(HttpServletRequest request, Model model) {
        String text = request.getParameter("textToBeParsed");

        Parser parser = new Parser(text);

        try {
            String parsedTextFinalOutput = String.join("\n", ParserUtil.flatten(ParserUtil.sortByAlphabet(parser.parse()), (ParseResult parseResult, int depth) -> ("-".repeat(depth) + " " + parseResult.getValue()).trim()));

            model.addAttribute("textToBeParsed", text);
            model.addAttribute("parsedTextFinalOutput", parsedTextFinalOutput);

            return "result";
        } catch (ParseException exception) {
            sessionUtil.flash(SESSION_ERROR_MESSAGE_PROPERTY_NAME, exception.getMessage());

            return "redirect:/";
        }
    }
}
