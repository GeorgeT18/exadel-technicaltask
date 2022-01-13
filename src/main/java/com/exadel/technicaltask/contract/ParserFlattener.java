package com.exadel.technicaltask.contract;

import com.exadel.technicaltask.model.ParseResult;

public interface ParserFlattener<T> {
    T op(ParseResult parseResult, int depth);
}
