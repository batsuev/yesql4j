package yesql4j.generator;

import yesql4j.parser.SQLQueryDefinition;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface IClassGenerator {

    @NotNull List<String> getImports();

    @NotNull List<String> generateSelect(@NotNull SQLQueryDefinition sqlQueryDefinition);
    @NotNull List<String> generateUpdate(@NotNull SQLQueryDefinition sqlQueryDefinition);
    @NotNull List<String> generateInsert(@NotNull SQLQueryDefinition sqlQueryDefinition);
}
