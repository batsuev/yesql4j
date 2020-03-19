package com.yesql4j.generator;

import com.yesql4j.parser.SQLQueryDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface IClassGenerator {

    @NotNull List<String> getImports();

    @NotNull List<String> getClassAnnotations();

    @NonNull String generateClassHeader(@NotNull String className);

    @NotNull List<String> generateSelect(@NotNull SQLQueryDefinition sqlQueryDefinition);

    @NotNull List<String> generateUpdate(@NotNull SQLQueryDefinition sqlQueryDefinition);

    @NotNull List<String> generateInsert(@NotNull SQLQueryDefinition sqlQueryDefinition);
}
