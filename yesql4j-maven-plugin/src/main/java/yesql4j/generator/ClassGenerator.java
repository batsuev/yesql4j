package yesql4j.generator;

import org.apache.commons.lang3.StringUtils;
import yesql4j.generator.variants.ReactorSQLClassGenerator;
import yesql4j.parser.SQLQueryDefinition;
import yesql4j.parser.SQLQueryType;

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
        }else {
            packageName = "package " + packageName + ";\n\n";
        }

        return String.format("%s" +
                        "%s" +
                        "\n" +
                        "\n" +
                        "public final class %s {\n" +
                        "\n" +
                        "%s" +
                        "\n" +
                        "}",
                packageName,
                imports,
                NameUtils.className(sqlSourceFile, sqlSourceRoot),
                StringUtils.join(methods, "\n\n"));
    }

    private IClassGenerator generatorForType(GenerationTarget target) {
        switch (target) {
            case VERTX_MYSQL_REACTOR:
                return new ReactorSQLClassGenerator();
        }
        throw new RuntimeException("Cannot find generator for type");
    }
}
