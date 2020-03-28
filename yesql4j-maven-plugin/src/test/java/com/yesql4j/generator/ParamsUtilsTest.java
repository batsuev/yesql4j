package com.yesql4j.generator;

import com.yesql4j.parser.SQLQueryDefinition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParamsUtilsTest {

    @Test
    void cleanupQuery() {
        String query = "SELECT name FROM test_table WHERE a = :a1 AND b = ? AND c = :a;";

        SQLQueryDefinition queryDefinition = new SQLQueryDefinition(
                "test",
                query
        );

        assertEquals(
                "SELECT name FROM test_table WHERE a = ? AND b = ? AND c = ?;",
                ParamsUtils.cleanupQuery(queryDefinition)
        );
    }

    @Test
    void getQueryParamsNamesAndBinding() {
        String query = "SELECT name FROM test_table WHERE a = :a AND b = ? AND c = :a AND d = :b AND e = ?;";

        SQLQueryDefinition queryDefinition = new SQLQueryDefinition(
                "test",
                query
        );

        assertEquals(
                Arrays.asList("a", "p0", "b", "p1"),
                ParamsUtils.getQueryParamsNames(queryDefinition)
        );

        assertEquals(
                Arrays.asList("a", "p0", "a", "b", "p1"),
                ParamsUtils.getQueryParamsBinding(queryDefinition)
        );
    }

    @Test
    void getParamsOffsets() {
        String query = "SELECT name FROM test_table WHERE a = :long_param AND b = ? AND c = :a AND d = :b AND e = ?;";
        SQLQueryDefinition queryDefinition = new SQLQueryDefinition(
                "test",
                query
        );

        assertEquals(
                Arrays.asList(38, 48, 57, 67, 77),
                ParamsUtils.getParamsOffsets(queryDefinition)
        );
    }
}