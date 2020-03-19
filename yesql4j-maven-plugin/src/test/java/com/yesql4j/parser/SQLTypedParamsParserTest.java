package com.yesql4j.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SQLTypedParamsParserTest {

    private final SQLParser parser = new SQLParser();

    private String resourceContent(String name) throws URISyntaxException, IOException {
        var path = Paths.get(getClass().getResource("/" + name).toURI());
        return new String(Files.readAllBytes(path));
    }

    @Test
    void parseParamTypes() throws URISyntaxException, IOException {
        var content = resourceContent("typed.sql");

        var queries = parser.parse(content);
        assertEquals(
                queries.get(0).getParamsTypes(),
                Map.of("name", "String", "id", "Long")
        );

        assertEquals(
                queries.get(1).getParamsTypes(),
                Map.of("id", "Long")
        );
    }
}
