package com.yesql4j.parser;

import com.yesql4j.antlr.YeSQLGrammarLexer;
import com.yesql4j.antlr.YeSQLGrammarParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
                    Map<String, String> paramsTypes = parseParamsTypes(parsed.param());
                    return new SQLQueryDefinition(name, body, paramsTypes);
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
        return body.replaceAll("\n[ ]*--.*\n$", "\n").trim();
    }

    private Map<String, String> parseParamsTypes(List<YeSQLGrammarParser.ParamContext> paramsContext) {
        return paramsContext.stream()
                .map(param -> param.getText().trim())
                .map(param -> StringUtils.removeStart(param, "--").trim())
                .map(param -> StringUtils.removeStart(param, "@param "))
                .map(param -> param.split("\\s+"))
                .collect(Collectors.toMap(
                        param -> param[param.length - 1],
                        param -> StringUtils.join(Arrays.copyOf(param, param.length - 1))
                ));
    }
}
