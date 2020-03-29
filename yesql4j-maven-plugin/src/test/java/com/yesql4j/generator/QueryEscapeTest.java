package com.yesql4j.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryEscapeTest {

    @Test
    void testSimpleCases() {
        assertEquals(
                "SELECT * FROM users WHERE id = ?;",
                SQLQueryEscaper.escape("SELECT * FROM users WHERE id = ?;")
        );

        assertEquals(
                "SELECT * FROM users WHERE id <> ?;",
                SQLQueryEscaper.escape("SELECT * FROM users WHERE id <> ?;")
        );

        assertEquals(
                "SELECT * \\nFROM users WHERE id =\\\"''\\\"",
                SQLQueryEscaper.escape("SELECT * \nFROM users WHERE id =\"''\"")
        );
    }
}
