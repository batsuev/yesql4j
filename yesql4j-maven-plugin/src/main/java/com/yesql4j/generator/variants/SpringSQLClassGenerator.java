package com.yesql4j.generator.variants;

import com.yesql4j.generator.EscapeUtils;
import com.yesql4j.generator.NameUtils;
import com.yesql4j.generator.ParamsUtils;
import com.yesql4j.parser.SQLQueryDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;
import com.yesql4j.generator.IClassGenerator;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpringSQLClassGenerator implements IClassGenerator {

    @Override
    public @NotNull List<String> getImports() {
        return List.of("org.springframework.dao.DataAccessException",
                "org.springframework.jdbc.core.JdbcTemplate",
                "org.springframework.lang.NonNull",
                "org.springframework.jdbc.core.RowMapper",
                "org.springframework.jdbc.support.GeneratedKeyHolder",
                "org.springframework.jdbc.support.KeyHolder",
                "org.springframework.stereotype.Component",
                "java.sql.PreparedStatement",
                "java.util.List;");
    }

    @Override
    public @NotNull List<String> getClassAnnotations() { return Collections.singletonList("@Component"); }

    @Override
    public @NonNull String generateClassHeader(@NotNull String className) {
        return String.format("    private final JdbcTemplate jdbcTemplate;\n" +
                "\n" +
                "    public %s(JdbcTemplate jdbcTemplate) {\n" +
                "        this.jdbcTemplate = jdbcTemplate;\n" +
                "    }\n", className);
    }

    @Override
    public @NotNull List<String> generateSelect(@NotNull SQLQueryDefinition sqlQueryDefinition) {
        var name = NameUtils.methodName(sqlQueryDefinition.getName());
        var body = EscapeUtils.escapedQuery(ParamsUtils.cleanupQuery(sqlQueryDefinition));

        if (sqlQueryDefinition.noParams()) {
            return List.of(
                    String.format("    @NonNull\n" +
                            "    public <T> List<T> %s(RowMapper<T> mapper) {\n" +
                            "        return jdbcTemplate.query(\"%s\", mapper);\n" +
                            "    }", name, body)
            );
        }else {
            return List.of(
                    String.format("    @NonNull\n" +
                            "    public <T> List<T> %s(%s, RowMapper<T> mapper) {\n" +
                            "        return jdbcTemplate.query(\"%s\", new Object[]{%s}, mapper);\n" +
                            "    }", name, ParamsUtils.methodParams(sqlQueryDefinition), body, paramsBindings(sqlQueryDefinition))
            );
        }
    }

    @Override
    public @NotNull List<String> generateUpdate(@NotNull SQLQueryDefinition sqlQueryDefinition) {
        var name = NameUtils.methodName(sqlQueryDefinition.getName());
        var body = EscapeUtils.escapedQuery(ParamsUtils.cleanupQuery(sqlQueryDefinition));

        if (sqlQueryDefinition.noParams()) {
            return List.of(
                    String.format("    @NonNull\n" +
                            "    public int %s() {\n" +
                            "        return jdbcTemplate.update(\"%s\");\n" +
                            "    }", name, body)
            );
        }

        return List.of(
                String.format("    @NonNull\n" +
                        "    public int %s(%s) {\n" +
                        "        return jdbcTemplate.update(\"%s\", %s);\n" +
                        "    }", name, ParamsUtils.methodParams(sqlQueryDefinition), body, paramsBindings(sqlQueryDefinition))
        );
    }

    @Override
    public @NotNull List<String> generateInsert(@NotNull SQLQueryDefinition sqlQueryDefinition) {
        var name = NameUtils.methodName(sqlQueryDefinition.getName());
        var body = EscapeUtils.escapedQuery(ParamsUtils.cleanupQuery(sqlQueryDefinition));

        if (sqlQueryDefinition.noParams()) {
            return List.of(
                    String.format("    @NonNull\n" +
                            "    public long %s() {\n" +
                            "        KeyHolder keyHolder = new GeneratedKeyHolder();\n" +
                            "\n" +
                            "        jdbcTemplate.update(connection -> connection.prepareStatement(%s), keyHolder);\n" +
                            "\n" +
                            "        return (long) keyHolder.getKey();" +
                            "    }", name, body)
            );
        }

        return List.of(
                String.format("    @NonNull\n" +
                        "    public long %s(%s) {\n" +
                        "        KeyHolder keyHolder = new GeneratedKeyHolder();\n" +
                        "\n" +
                        "        jdbcTemplate.update(connection -> {\n" +
                        "            PreparedStatement ps = connection.prepareStatement(\"%s\");\n" +
                        "%s\n" +
                        "            return ps;\n" +
                        "        }, keyHolder);\n" +
                        "\n" +
                        "        return (long) keyHolder.getKey();" +
                        "    }", name, ParamsUtils.methodParams(sqlQueryDefinition), body, psParamsBindings(sqlQueryDefinition))
        );
    }

    private static String paramsBindings(SQLQueryDefinition queryDefinition) {
        return String.join(", ", ParamsUtils.getQueryParamsBinding(queryDefinition));
    }

    private static String psParamsBindings(SQLQueryDefinition queryDefinition) {
        ArrayList<String> res = new ArrayList<>();
        List<String> bindings = ParamsUtils.getQueryParamsBinding(queryDefinition);
        for (int i = 0; i < bindings.size(); i++) {
            String param = bindings.get(i);
            res.add(String.format("            ps.setObject(%d, %s);", i+1, param));
        }
        return String.join("\n", res);
    }
}
