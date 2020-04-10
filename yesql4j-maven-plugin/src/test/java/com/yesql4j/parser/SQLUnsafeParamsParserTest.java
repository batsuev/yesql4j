package com.yesql4j.parser;

import com.yesql4j.generator.ParamsUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SQLUnsafeParamsParserTest {

    private final SQLParser parser = new SQLParser();

    private String resourceContent(String name) throws URISyntaxException, IOException {
        var path = Paths.get(getClass().getResource("/" + name).toURI());
        return new String(Files.readAllBytes(path));
    }

    @Test
    void typesOk() throws URISyntaxException, IOException {
        var content = resourceContent("unsafe_params.sql");

        var queries = parser.parse(content);
        assertEquals(
                queries.get(0).getParamsTypes(),
                Map.of("id", "Long", "sorting", "String")
        );
    }

    @Test
    void cleanupOk() throws IOException, URISyntaxException {
        var content = resourceContent("unsafe_params.sql");

        var queries = parser.parse(content);

        assertEquals("SELECT * FROM users WHERE id > ? ?;", ParamsUtils.cleanupQuery(queries.get(0)));
    }

    @Test
    void signatureOk() throws IOException, URISyntaxException {
        var content = resourceContent("unsafe_params.sql");

        var queries = parser.parse(content);
        assertEquals(
                Arrays.asList("id", "sorting"),
                ParamsUtils.getQueryParamsNames(queries.get(0))
        );

        assertEquals(
                Collections.singletonList("id"),
                ParamsUtils.getQueryParamsBinding(queries.get(0))
        );
    }
}
