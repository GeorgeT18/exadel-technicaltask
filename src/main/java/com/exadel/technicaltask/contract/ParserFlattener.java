package com.exadel.technicaltask.contract;

import com.exadel.technicaltask.model.ParseResult;

public interface ParserFlattener {
    Object op(ParseResult parseResult, int depth);
}
