package com.yesql4j.parser;

import com.yesql4j.antlr.YeSQLGrammarLexer;
import com.yesql4j.antlr.YeSQLGrammarParser;
import com.yesql4j.parser.params.SQLParam;
import com.yesql4j.parser.params.SQLParamsFinder;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
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
                    List<SQLParam> params = SQLParamsFinder.search(body);
                    Map<String, String> paramsTypes = parseParamsTypes(parsed.param(), params);
                    return new SQLQueryDefinition(name, body, params, paramsTypes);
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

    private Map<String, String> parseParamsTypes(List<YeSQLGrammarParser.ParamContext> paramsContext, List<SQLParam> params) {
        Map<String, String> unsafeParams = params.stream()
                .filter(SQLParam::isUnsafe)
                .collect(Collectors.toMap(SQLParam::getName, (val) -> "String"));

        Map<String, String> normalParams = paramsContext.stream()
                .map(param -> param.getText().trim())
                .map(param -> StringUtils.removeStart(param, "--").trim())
                .map(param -> StringUtils.removeStart(param, "@param "))
                .map(param -> param.split("\\s+"))
                .collect(Collectors.toMap(
                        param -> param[param.length - 1],
                        param -> StringUtils.join(Arrays.copyOf(param, param.length - 1))
                ));

        HashMap<String, String> res = new HashMap<>();
        res.putAll(unsafeParams);
        res.putAll(normalParams);

        return res;
    }
}
