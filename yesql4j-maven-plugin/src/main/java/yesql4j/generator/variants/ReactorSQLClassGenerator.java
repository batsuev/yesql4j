package yesql4j.generator.variants;

import yesql4j.generator.EscapeUtils;
import yesql4j.generator.IClassGenerator;
import yesql4j.generator.NameUtils;
import yesql4j.generator.ParamsUtils;
import yesql4j.parser.SQLQueryDefinition;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class ReactorSQLClassGenerator implements IClassGenerator {

    private final List<String> imports = List.of(
            "reactor.core.publisher.Mono",
            "yesql4j.reactor.Yesql4jReactor",
            "io.vertx.mysqlclient.MySQLClient",
            "io.vertx.mysqlclient.MySQLPool",
            "io.vertx.sqlclient.PoolOptions",
            "io.vertx.sqlclient.SqlResult",
            "io.vertx.sqlclient.Tuple",
            "io.vertx.sqlclient.Row",
            "io.vertx.sqlclient.RowSet",
            "javax.validation.constraints.NotNull",
            "java.util.List",
            "java.util.Map",
            "java.util.stream.Collectors"
    );

    @Override
    public @NotNull List<String> getImports() {
        return imports;
    }

    @Override
    public @NotNull List<String> generateSelect(@NotNull SQLQueryDefinition sqlQueryDefinition) {
        var name = NameUtils.methodName(sqlQueryDefinition.getName());
        var body = EscapeUtils.escapedQuery(ParamsUtils.cleanupQuery(sqlQueryDefinition));

        if (sqlQueryDefinition.noParams()) {
            return List.of(
                    String.format("    @NotNull\n" +
                            "    public static Mono<RowSet<Row>> %s(MySQLPool pool) {\n" +
                            "        return Yesql4jReactor.preparedQuery(pool, \"%s\");\n" +
                            "    }", name, body)
            );
        }

        return List.of(
                String.format("    @NotNull\n" +
                        "    public static Mono<RowSet<Row>> %s(MySQLPool pool, %s) {\n" +
                        "        return Yesql4jReactor.preparedQuery(pool, \"%s\", Tuple.wrap(%s));\n" +
                        "    }", name, methodParams(sqlQueryDefinition), body, paramsBindings(sqlQueryDefinition))
        );
    }

    @Override
    public @NotNull List<String> generateUpdate(@NotNull SQLQueryDefinition sqlQueryDefinition) {
        var name = NameUtils.methodName(sqlQueryDefinition.getName());
        var body = EscapeUtils.escapedQuery(ParamsUtils.cleanupQuery(sqlQueryDefinition));

        if (sqlQueryDefinition.noParams()) {
            return List.of(
                    String.format("    @NotNull\n" +
                            "    public static Mono<Integer> %s(MySQLPool pool) {\n" +
                            "        return Yesql4jReactor.preparedQuery(pool, \"%s\").map(SqlResult::rowCount);\n" +
                            "    }", name, body)
            );
        }

        return List.of(
                String.format("    @NotNull\n" +
                        "    public static Mono<Integer> %s(MySQLPool pool, %s) {\n" +
                        "        return Yesql4jReactor.preparedQuery(pool, \"%s\", Tuple.wrap(%s)).map(SqlResult::rowCount);\n" +
                        "    }", name, methodParams(sqlQueryDefinition), body, paramsBindings(sqlQueryDefinition)),
                String.format("    @NotNull\n" +
                        "    public static Mono<Integer> %s(MySQLPool pool, List<Map<String, Object>> params) {\n" +
                        "        List<Tuple> wrappedParams = params.stream()\n" +
                        "                .map(row -> Tuple.wrap(%s))\n" +
                        "                .collect(Collectors.toList());\n" +
                        "        return Yesql4jReactor.preparedBatch(pool, \"%s\", wrappedParams).map(SqlResult::rowCount);\n" +
                        "    }", name, mapParamsBindings(sqlQueryDefinition), body)
        );
    }

    @Override
    public @NotNull List<String> generateInsert(@NotNull SQLQueryDefinition sqlQueryDefinition) {
        var name = NameUtils.methodName(sqlQueryDefinition.getName());
        var body = EscapeUtils.escapedQuery(ParamsUtils.cleanupQuery(sqlQueryDefinition));

        if (sqlQueryDefinition.noParams()) {
            return List.of(
                    String.format("    @NotNull\n" +
                            "    public static Mono<Integer> %s(MySQLPool pool) {\n" +
                            "        return Yesql4jReactor.preparedQuery(pool, \"%s\").map(res -> res.property(MySQLClient.LAST_INSERTED_ID);\n" +
                            "    }", name, body)
            );
        }

        return List.of(
                String.format("    @NotNull\n" +
                        "    public static Mono<Long> %s(MySQLPool pool, %s) {\n" +
                        "        return Yesql4jReactor.preparedQuery(pool, \"%s\", Tuple.wrap(%s)).map(res -> res.property(MySQLClient.LAST_INSERTED_ID));\n" +
                        "    }", name, methodParams(sqlQueryDefinition), body, paramsBindings(sqlQueryDefinition))
        );
    }

    private static String methodParams(SQLQueryDefinition queryDefinition) {
        return ParamsUtils.getQueryParamsNames(queryDefinition).stream()
                .map(el -> "Object " + el)
                .collect(Collectors.joining(", "));
    }

    private static String paramsBindings(SQLQueryDefinition queryDefinition) {
        return String.join(", ", ParamsUtils.getQueryParamsBinding(queryDefinition));
    }

    private static String mapParamsBindings(SQLQueryDefinition queryDefinition) {
        return ParamsUtils.getQueryParamsBinding(queryDefinition).stream()
                .map(el -> "row.get(\""+el+"\")")
                .collect(Collectors.joining(", "));
    }
}
