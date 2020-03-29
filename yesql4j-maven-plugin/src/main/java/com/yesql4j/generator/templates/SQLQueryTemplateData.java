package com.yesql4j.generator.templates;

import com.yesql4j.generator.NameUtils;
import com.yesql4j.generator.ParamsUtils;
import com.yesql4j.generator.SQLQueryEscaper;
import com.yesql4j.parser.SQLQueryDefinition;

import java.util.stream.Collectors;

public final class SQLQueryTemplateData {

    private final String escapedQuery;
    private final String paramsSignature;
    private final String paramsIndexes;
    private final String paramsBindings;
    private final String name;
    private final Boolean hasParams;

    public static SQLQueryTemplateData create(SQLQueryDefinition sqlQueryDefinition) {
        return new SQLQueryTemplateData(sqlQueryDefinition);
    }

    private SQLQueryTemplateData(SQLQueryDefinition sqlQueryDefinition) {
        this.escapedQuery = SQLQueryEscaper.escape(ParamsUtils.cleanupQuery(sqlQueryDefinition));
        this.name = NameUtils.methodName(sqlQueryDefinition.getName());
        this.paramsSignature = ParamsUtils.methodParams(sqlQueryDefinition);
        this.paramsIndexes = ParamsUtils.getParamsOffsets(sqlQueryDefinition)
                .stream().map(Object::toString).collect(Collectors.joining(", "));
        this.paramsBindings = String.join(",", ParamsUtils.getQueryParamsBinding(sqlQueryDefinition));
        this.hasParams = sqlQueryDefinition.hasParams();
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return escapedQuery;
    }

    public String getParamsBindings() {
        return paramsBindings;
    }

    public String getParamsIndexes() {
        return paramsIndexes;
    }

    public String getParamsSignature() {
        return paramsSignature;
    }

    public Boolean getHasParams() {
        return hasParams;
    }

    @Override
    public String toString() {
        return "SQLQueryTemplateData{" +
                "escapedQuery='" + escapedQuery + '\'' +
                ", paramsSignature='" + paramsSignature + '\'' +
                ", paramsIndexes='" + paramsIndexes + '\'' +
                ", paramsBindings='" + paramsBindings + '\'' +
                ", name='" + name + '\'' +
                ", hasParams=" + hasParams +
                '}';
    }
}
