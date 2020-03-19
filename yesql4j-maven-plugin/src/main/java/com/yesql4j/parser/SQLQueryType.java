package com.yesql4j.parser;

import org.apache.commons.lang3.StringUtils;

public enum SQLQueryType {
    SELECT,
    INSERT,
    UPDATE;

    public static SQLQueryType forName(String name) {
        if (StringUtils.endsWith(name, "<!"))
            return SQLQueryType.INSERT;
        if (StringUtils.endsWith(name, "!"))
            return SQLQueryType.UPDATE;
        return SQLQueryType.SELECT;
    }
}
