package com.yesql4j.parser;

import com.yesql4j.parser.params.SQLParam;
import com.yesql4j.parser.params.SQLParamsFinder;
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

        List<SQLParam> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList(
                        SQLParam.create("name", 45),
                        SQLParam.create("age", 53)
                ),
                params
        );
    }

    @Test
    void searchMixedParams() {
        String query = "SELECT\n" +
                "    :a + 1 adder,\n" +
                "    ? - 1 subtractor\n" +
                "FROM SYSIBM.SYSDUMMY1;";

        List<SQLParam> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList(
                        SQLParam.create("a", 11),
                        SQLParam.create("?", 29)
                ),
                params
        );
    }

    @Test
    void searchParamsReuse() {
        String query = "SELECT test FROM table WHERE a = :a AND b = :a;";
        List<SQLParam> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList(
                        SQLParam.create("a", 33),
                        SQLParam.create("a", 44)
                ),
                params
        );
    }

    @Test
    void skipQuotedParams() {
        String query = "SELECT test FROM table WHERE a = ':a' AND b = \":a\";";
        List<SQLParam> params = SQLParamsFinder.search(query);
        assertTrue(params.isEmpty());
    }

    @Test
    void paramFormatOk() {
        String query = "SELECT name FROM test_table WHERE a = :a1 AND b = ?; AND c = :a OR d = :e_f";
        List<SQLParam> params = SQLParamsFinder.search(query);
        assertEquals(
                Arrays.asList(
                        SQLParam.create("a1", 38),
                        SQLParam.create("?", 50),
                        SQLParam.create("a", 61),
                        SQLParam.create("e_f", 71)
                ),
                params
        );
    }
}