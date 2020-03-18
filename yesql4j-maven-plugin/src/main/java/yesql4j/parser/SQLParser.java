package yesql4j.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import yesql4j.antlr.YeSQLGrammarLexer;
import yesql4j.antlr.YeSQLGrammarParser;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SQLParser {

    public List<SQLQueryDefinition> parse(String sql) {
        var queries = sql.trim() + "\n";
        var lexer = new YeSQLGrammarLexer(CharStreams.fromString(queries));
        var parser = new YeSQLGrammarParser(new CommonTokenStream(lexer));

        return parser.queries().query().stream()
                .map(parsed -> {
                    String name = cleanupName(parsed.name().getText());
                    String body = cleanupBody(parsed.statement().getText());
                    return new SQLQueryDefinition(name, body);
                })
                .collect(Collectors.toList());
    }

    private String cleanupName(String nameDefinition) {
        return StringUtils.removeStart(
                nameDefinition
                        .trim()
                        .replaceAll("\\s+", ""),
                "--name:"
        );
    }

    private String cleanupBody(String body) {
        return body.replaceAll("\n[ ]*--.*\n$","\n").trim();
    }
}
