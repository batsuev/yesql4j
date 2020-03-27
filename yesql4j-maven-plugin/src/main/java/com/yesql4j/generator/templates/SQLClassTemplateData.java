package com.yesql4j.generator.templates;

import java.util.List;

public final class SQLClassTemplateData {

    private final String className;
    private final String packageName;

    private final List<SQLQueryTemplateData> selects;
    private final List<SQLQueryTemplateData> updates;
    private final List<SQLQueryTemplateData> inserts;

    public SQLClassTemplateData(String className,
                                String packageName,
                                List<SQLQueryTemplateData> selects,
                                List<SQLQueryTemplateData> updates,
                                List<SQLQueryTemplateData> inserts) {
        this.className = className;
        this.packageName = packageName;
        this.selects = selects;
        this.updates = updates;
        this.inserts = inserts;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<SQLQueryTemplateData> getInserts() {
        return inserts;
    }

    public List<SQLQueryTemplateData> getSelects() {
        return selects;
    }

    public List<SQLQueryTemplateData> getUpdates() {
        return updates;
    }

    @Override
    public String toString() {
        return "SQLClassTemplateData{" +
                "className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", selects=" + selects +
                ", updates=" + updates +
                ", inserts=" + inserts +
                '}';
    }
}
