package yesql4j.parser;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public final class SQLQueryDefinition {
    @NotNull
    private final String name;
    @NotNull
    private final String query;
    @NotNull
    private final List<String> params;

    public SQLQueryDefinition(@NotNull String name, @NotNull String query) {
        this(name, query, SQLParamsFinder.search(query));
    }

    public SQLQueryDefinition(@NotNull String name, @NotNull String query, @NotNull List<String> params) {
        this.name = name;
        this.query = query;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public List<String> getParams() {
        return params;
    }

    public boolean noParams() {
        return this.params.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLQueryDefinition that = (SQLQueryDefinition) o;
        return name.equals(that.name) && query.equals(that.query) && params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, query, params);
    }

    @Override
    public String toString() {
        return String.format("SQLQueryDefinition[name='%s', query='%s', params='%s']", name, query, params);
    }
}
