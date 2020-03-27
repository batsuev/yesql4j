package com.yesql4j.parser;

import com.yesql4j.parser.params.SQLParam;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SQLParserTest {

    private final SQLParser parser = new SQLParser();

    private String resourceContent(String name) throws URISyntaxException, IOException {
        var path = Paths.get(getClass().getResource("/" + name).toURI());
        return new String(Files.readAllBytes(path));
    }

    @Test
    void parseAcceptanceCases() throws URISyntaxException, IOException {
        var content = resourceContent("acceptance_test_combined.sql");

        var queries = parser.parse(content);
        assertEquals(7, queries.size(), "read all queries");

        assertEquals(
                new SQLQueryDefinition(
                        "create-person-table!",
                        "CREATE TABLE person (\n" +
                                "\tperson_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,\n" +
                                "\tname VARCHAR(20) UNIQUE NOT NULL,\n" +
                                "\tage INTEGER NOT NULL\n" +
                                ");",
                        Collections.emptyList()),
                queries.get(0));

        assertEquals(
                new SQLQueryDefinition(
                        "insert-person<!",
                        "INSERT INTO person (\n" +
                                "\tname,\n" +
                                "\tage\n" +
                                ") VALUES (\n" +
                                "\t:name,\n" +
                                "\t:age\n" +
                                ");",
                        Arrays.asList(
                                SQLParam.create("name", 45),
                                SQLParam.create("age", 53)
                        )),
                queries.get(1));

        assertEquals(
                new SQLQueryDefinition(
                        "find-older-than",
                        "SELECT *\n" +
                                "FROM person\n" +
                                "WHERE age > :age;",
                        Collections.singletonList(
                                SQLParam.create("age", 33)
                        )),
                queries.get(2));

        assertEquals(
                new SQLQueryDefinition(
                        "find-by-age",
                        "SELECT *\n" +
                                "FROM person\n" +
                                "WHERE age IN (:age);",
                        Collections.singletonList(
                                SQLParam.create("age", 35)
                        )),
                queries.get(3));

        assertEquals(
                new SQLQueryDefinition(
                        "update-age!",
                        "UPDATE person\n" +
                                "SET age = :age\n" +
                                "WHERE name = :name;",
                        Arrays.asList(
                                SQLParam.create("age", 24),
                                SQLParam.create("name", 42)
                        )),
                queries.get(4));

        assertEquals(
                new SQLQueryDefinition(
                        "delete-person!",
                        "DELETE FROM person\n" +
                                "WHERE name = :name;",
                        Collections.singletonList(
                                SQLParam.create("name", 32)
                        )),
                queries.get(5));

        assertEquals(
                new SQLQueryDefinition(
                        "drop-person-table!",
                        "DROP TABLE person;",
                        Collections.emptyList()
                ),
                queries.get(6)
        );
    }

    @Test
    void parseCombinedFile() throws IOException, URISyntaxException {
        var content = resourceContent("combined_file.sql");

        var queries = parser.parse(content);
        assertEquals(3, queries.size());

        assertEquals(
                new SQLQueryDefinition(
                        "the-time",
                        "SELECT CURRENT_TIMESTAMP\n" +
                                "FROM SYSIBM.SYSDUMMY1;",
                        Collections.emptyList()
                ),
                queries.get(0)
        );

        assertEquals(
                new SQLQueryDefinition(
                        "sums",
                        "SELECT\n" +
                                "    :a + 1 adder,\n" +
                                "    :b - 1 subtractor\n" +
                                "FROM SYSIBM.SYSDUMMY1;",
                        Arrays.asList(
                                SQLParam.create("a", 11),
                                SQLParam.create("b", 29)
                        )
                ),
                queries.get(1)
        );

        assertEquals(
                new SQLQueryDefinition(
                        "edge",
                        "SELECT\n" +
                                "    1 + 1 AS two\n" +
                                "    -- I find this query dull.\n" +
                                "FROM SYSIBM.SYSDUMMY1;",
                        Collections.emptyList()
                ),
                queries.get(2)
        );
    }

    @Test
    void testComplicatedDocString() throws IOException, URISyntaxException {
        var content = resourceContent("complicated_docstring.sql");

        var queries = parser.parse(content);
        assertEquals(3, queries.size());

        assertEquals(
                new SQLQueryDefinition(
                        "test1",
                        "SELECT CURRENT_TIMESTAMP AS time\n" +
                                "FROM SYSIBM.SYSDUMMY1;",
                        Collections.emptyList()
                ),
                queries.get(0)
        );

        assertEquals(
                new SQLQueryDefinition(
                        "test-special1",
                        "SELECT a where b = ';';",
                        Collections.emptyList()
                ),
                queries.get(1)
        );

        assertEquals(
                new SQLQueryDefinition(
                        "test-special2",
                        "SELECT a\n" +
                                "  where\n" +
                                "    b = ';' and\n" +
                                "    d = ';';",
                        Collections.emptyList()
                ),
                queries.get(2)
        );
    }

    @Test
    void testInlineComment() throws IOException, URISyntaxException {
        var content = resourceContent("inline_comment.sql");

        var queries = parser.parse(content);
        assertEquals(1, queries.size());

        assertEquals(
                new SQLQueryDefinition(
                        "test",
                        "SELECT\n" +
                                " CURRENT_TIMESTAMP AS time, -- Here is an inline comment.\n" +
                                " 'Not -- a comment' AS string\n" +
                                "FROM SYSIBM.SYSDUMMY1;",
                        Collections.emptyList()
                ),
                queries.get(0)
        );
    }

    @Test
    void testParserEdgeCases() throws IOException, URISyntaxException {
        var content = resourceContent("parser_edge_cases.sql");

        var queries = parser.parse(content);
        assertEquals(1, queries.size());

        assertEquals(
                new SQLQueryDefinition(
                        "this-has-trailing-whitespace",
                        "SELECT CURRENT_TIMESTAMP\n" +
                                "FROM SYSIBM.SYSDUMMY1;",
                        Collections.emptyList()
                ),
                queries.get(0)
        );
    }
}