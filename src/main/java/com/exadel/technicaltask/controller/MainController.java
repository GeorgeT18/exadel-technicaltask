package com.exadel.technicaltask.controller;

import com.exadel.technicaltask.exception.parser.ParseException;
import com.exadel.technicaltask.model.ParseResult;
import com.exadel.technicaltask.parser.Parser;
import com.exadel.technicaltask.util.ParserUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class MainController {

    @GetMapping
    public String home(Model model, HttpSession session) {
        model.addAttribute("defaultTextToBeParsed", "(id,created,employee(id,firstname,employeeType(id),lastname),location)");

        String errorMessage = null;

        final String errorMessagePropertyName = "errorMessage";

        if (session.getAttribute(errorMessagePropertyName) != null) {
            errorMessage = session.getAttribute(errorMessagePropertyName).toString();
        }

        session.setAttribute(errorMessagePropertyName, null);

        model.addAttribute(errorMessagePropertyName, errorMessage);

        return "home";
    }

    @PostMapping
    public String submit(HttpServletRequest request, Model model, HttpSession session) {
        String text = request.getParameter("textToBeParsed");

        Parser parser = new Parser(text);

        try {
            String parsedTextFinalOutput = ParserUtil.flatten(ParserUtil.sortByAlphabet(parser.parse()), (ParseResult parseResult, int depth) -> ("-".repeat(depth) + " " + parseResult.getValue()).trim()).stream().map(Object::toString).collect(Collectors.joining("\n"));

            model.addAttribute("textToBeParsed", text);
            model.addAttribute("parsedTextFinalOutput", parsedTextFinalOutput);

            return "result";
        } catch (ParseException exception) {
            session.setAttribute("errorMessage", exception.getMessage());

            return "redirect:/";
        }
    }
}
