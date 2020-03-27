package com.yesql4j.generator;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.yesql4j.generator.templates.SQLClassTemplateData;
import com.yesql4j.generator.templates.SQLQueryTemplateData;
import com.yesql4j.parser.SQLQueryDefinition;
import com.yesql4j.parser.SQLQueryType;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class ClassGenerator {

    private final TemplateLoader templateLoader = new ClassPathTemplateLoader();
    private final Handlebars handlebars;

    public ClassGenerator() {
        templateLoader.setPrefix("/");
        templateLoader.setSuffix(".java");

        handlebars = new Handlebars(templateLoader);
    }

    @NotNull
    public String generate(
            @NotNull Path sqlSourceFile,
            @NotNull Path sqlSourceRoot,
            @NotNull List<SQLQueryDefinition> queries,
            @NotNull GenerationTarget target) throws IOException {

        var template = templateForType(target);

        var templateData = new SQLClassTemplateData(
                NameUtils.className(sqlSourceFile, sqlSourceRoot),
                NameUtils.packageName(sqlSourceFile, sqlSourceRoot),
                queries.stream()
                        .filter(q -> SQLQueryType.forName(q.getName()) == SQLQueryType.SELECT)
                        .map(SQLQueryTemplateData::create)
                        .collect(Collectors.toList()),
                queries.stream()
                        .filter(q -> SQLQueryType.forName(q.getName()) == SQLQueryType.UPDATE)
                        .map(SQLQueryTemplateData::create)
                        .collect(Collectors.toList()),
                queries.stream()
                        .filter(q -> SQLQueryType.forName(q.getName()) == SQLQueryType.INSERT)
                        .map(SQLQueryTemplateData::create)
                        .collect(Collectors.toList())
        );

        return template.apply(templateData);
    }

    private Template templateForType(GenerationTarget target) throws IOException {
        switch (target) {
            case VERTX_MYSQL_REACTOR:
                return handlebars.compile("reactor");
        }
        throw new RuntimeException("Cannot find generator for type");
    }
}
