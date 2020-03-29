package com.yesql4j.parser;

import com.yesql4j.generator.ClassGenerator;
import com.yesql4j.generator.GenerationTarget;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SQLClassGeneratorsTest {

    private final SQLParser parser = new SQLParser();
    private final ClassGenerator classGenerator = new ClassGenerator();

    private String resourceContent(String name) throws URISyntaxException, IOException {
        var path = Paths.get(getClass().getResource("/" + name).toURI());
        return new String(Files.readAllBytes(path));
    }

    @Test
    void reactorGenerationOk() throws IOException, URISyntaxException {
        var content = resourceContent("acceptance_test_combined.sql");

        var queries = parser.parse(content);
        var generated = classGenerator.generate(
                Path.of("sql", "acceptance_test_combined.sql"),
                Path.of(""),
                queries,
                GenerationTarget.VERTX_MYSQL_REACTOR);

        System.out.println("generated: " + generated);
    }

    @Test
    void springGenerationOk() throws IOException, URISyntaxException {
        var content = resourceContent("acceptance_test_combined.sql");

        var queries = parser.parse(content);
        var generated = classGenerator.generate(
                Path.of("sql", "acceptance_test_combined.sql"),
                Path.of(""),
                queries,
                GenerationTarget.SPRING);

        System.out.println("generated: " + generated);
    }
}
