package com.exadel.technicaltask.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParseResult {
    private String value;
    private List<ParseResult> children;
}
