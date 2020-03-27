package com.yesql4j.parser;

import com.yesql4j.parser.params.SQLParam;
import com.yesql4j.parser.params.SQLParamsFinder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SQLQueryDefinition {
    @NotNull
    private final String name;
    @NotNull
    private final String query;
    @NotNull
    private final List<SQLParam> params;
    @NotNull
    private final Map<String, String> paramsTypes;

    public SQLQueryDefinition(@NotNull String name, @NotNull String query) {
        this(name, query, SQLParamsFinder.search(query), Collections.emptyMap());
    }

    public SQLQueryDefinition(@NotNull String name, @NotNull String query, @NotNull Map<String, String> paramsTypes) {
        this(name, query, SQLParamsFinder.search(query), paramsTypes);
    }

    public SQLQueryDefinition(@NotNull String name, @NotNull String query, @NotNull List<SQLParam> params) {
        this(name, query, params, Collections.emptyMap());
    }

    public SQLQueryDefinition(@NotNull String name, @NotNull String query, @NotNull List<SQLParam> params, Map<String, String> paramsTypes) {
        this.name = name;
        this.query = query;
        this.params = params;
        this.paramsTypes = paramsTypes;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public List<SQLParam> getParams() {
        return params;
    }

    public Map<String, String> getParamsTypes() {
        return paramsTypes;
    }

    public boolean noParams() {
        return this.params.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLQueryDefinition that = (SQLQueryDefinition) o;
        return name.equals(that.name) &&
                query.equals(that.query) &&
                params.equals(that.params) &&
                paramsTypes.equals(that.paramsTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, query, params);
    }

    @Override
    public String toString() {
        return String.format("SQLQueryDefinition[name='%s', query='%s', params='%s', types='%s']", name, query, params, paramsTypes);
    }
}
