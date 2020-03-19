package com.yesql4j.parser;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLParamsFinderTest {

    @Test
    void searchBasicParams() {
        String query = "INSERT INTO person (\n" +
                "\tname,\n" +
                "\tage\n" +
                ") VALUES (\n" +
                "\t:name,\n" +
                "\t:age\n" +
                ");";

        List<String> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList("name", "age"),
                params
        );
    }

    @Test
    void searchMixedParams() {
        String query = "SELECT\n" +
                "    :a + 1 adder,\n" +
                "    ? - 1 subtractor\n" +
                "FROM SYSIBM.SYSDUMMY1;";

        List<String> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList("a", "?"),
                params
        );
    }

    @Test
    void searchParamsReuse() {
        String query = "SELECT test FROM table WHERE a = :a AND b = :a;";
        List<String> params = SQLParamsFinder.search(query);
        assertEquals(2, params.size());
        assertEquals("a", params.get(0));
        assertEquals("a", params.get(1));
    }

    @Test
    void skipQuotedParams() {
        String query = "SELECT test FROM table WHERE a = ':a' AND b = \":a\";";
        List<String> params = SQLParamsFinder.search(query);
        assertTrue(params.isEmpty());
    }

    @Test
    void paramFormatOk() {
        String query = "SELECT name FROM test_table WHERE a = :a1 AND b = ?; AND c = :a OR d = :e_f";
        List<String> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList("a1", "?", "a", "e_f"),
                params
        );
    }
}