package com.exadel.technicaltask.util;

import com.exadel.technicaltask.contract.ParserFlattener;
import com.exadel.technicaltask.model.ParseResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ParserUtil {
    private ParserUtil() {}

    private static void flatten(List<ParseResult> parseResultList, ParserFlattener parserFlattener, List<Object> appendToFlatList, int currentDepth) {
        for (ParseResult parseResult : parseResultList) {
            appendToFlatList.add(parserFlattener.op(parseResult, currentDepth));

            if (parseResult.getChildren() != null) {
                ParserUtil.flatten(parseResult.getChildren(), parserFlattener, appendToFlatList, currentDepth + 1);
            }
        }
    }

    public static List<Object> flatten(List<ParseResult> parseResultList, ParserFlattener parserFlattener) {
        List<Object> flatList = new ArrayList<>();

        ParserUtil.flatten(parseResultList, parserFlattener, flatList, 0);

        return flatList;
    }


    public static List<ParseResult> sortByAlphabet(List<ParseResult> parseResultList) {
        List<ParseResult> parseResultListSorted = parseResultList.stream().sorted(Comparator.comparing(ParseResult::getValue)).toList();

        for (ParseResult parseResult : parseResultListSorted) {
            if (parseResult.getChildren() != null) {
                parseResult.setChildren(ParserUtil.sortByAlphabet(parseResult.getChildren()));
            }
        }

        return parseResultListSorted;
    }
}
