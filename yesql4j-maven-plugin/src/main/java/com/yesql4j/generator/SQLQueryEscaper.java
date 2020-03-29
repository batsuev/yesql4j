package com.yesql4j.generator;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.Map;

public class SQLQueryEscaper {

    private static AggregateTranslator ESCAPER;

    static {
        ESCAPER = new AggregateTranslator(
                new LookupTranslator(Map.of("\"", "\\\"", "\\", "\\\\")),
                new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE)
        );
    }

    public static String escape(String query) {
        return ESCAPER.translate(query);
    }
}
