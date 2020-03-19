package com.yesql4j.generator;

import com.yesql4j.parser.SQLQueryDefinition;
import com.yesql4j.parser.SQLQueryType;
import org.apache.commons.lang3.StringUtils;
import com.yesql4j.generator.variants.ReactorSQLClassGenerator;
import com.yesql4j.generator.variants.SpringSQLClassGenerator;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ClassGenerator {

    @NotNull
    public String generate(
            @NotNull Path sqlSourceFile,
            @NotNull Path sqlSourceRoot,
            @NotNull List<SQLQueryDefinition> queries,
            @NotNull GenerationTarget target) {

        var generator = generatorForType(target);

        ArrayList<String> methods = new ArrayList<>();
        queries.forEach(query -> {
            var sqlQueryType = SQLQueryType.forName(query.getName());
            switch (sqlQueryType) {
                case SELECT:
                    methods.addAll(generator.generateSelect(query));
                    break;
                case INSERT:
                    methods.addAll(generator.generateInsert(query));
                    break;
                case UPDATE:
                    methods.addAll(generator.generateUpdate(query));
                    break;
            }
        });

        var imports = generator.getImports().stream().map(p -> String.format("import %s;", p))
                .collect(Collectors.joining("\n"));

        var packageName = NameUtils.packageName(sqlSourceFile, sqlSourceRoot);
        if (packageName.isEmpty()) {
            packageName = "";
        } else {
            packageName = "package " + packageName + ";\n\n";
        }

        String className = NameUtils.className(sqlSourceFile, sqlSourceRoot);

        return String.format("%s" +
                        "%s" +
                        "\n" +
                        "\n" +
                        "%s" +
                        "public final class %s {\n" +
                        "\n" +
                        "%s" +
                        "\n" +
                        "%s" +
                        "\n" +
                        "}",
                packageName,
                imports,
                generator.getClassAnnotations().stream().map(el -> el + "\n").collect(Collectors.joining()),
                className,
                generator.generateClassHeader(className),
                StringUtils.join(methods, "\n\n"));
    }

    private IClassGenerator generatorForType(GenerationTarget target) {
        switch (target) {
            case VERTX_MYSQL_REACTOR:
                return new ReactorSQLClassGenerator();
            case SPRING:
                return new SpringSQLClassGenerator();
        }
        throw new RuntimeException("Cannot find generator for type");
    }
}
