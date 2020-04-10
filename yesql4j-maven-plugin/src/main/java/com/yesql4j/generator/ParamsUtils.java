package com.yesql4j.generator;

import com.yesql4j.parser.SQLQueryDefinition;
import com.yesql4j.parser.params.SQLParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ParamsUtils {

    public static String cleanupQuery(SQLQueryDefinition query) {
        StringBuilder res = new StringBuilder(query.getQuery());
        int offset = 0;
        for (SQLParam param : query.getParams()) {
            if (param.isNamed()) {
                SQLParam offseted = param.offsetLeft(offset);
                res.replace(offseted.getStartIndex(), offseted.getEndIndex(), "?");
                offset += offseted.getNameLength() - 1;
            }
        }

        return res.toString();
    }

    public static List<String> getQueryParamsNames(SQLQueryDefinition query) {
        int lastQIndex = 0;
        ArrayList<String> res = new ArrayList<>();
        for (SQLParam param : query.getParams()) {
            if (param.isNamed()) {
                if (!res.contains(param.getName()))
                    res.add(param.getName());
            } else {
                res.add("p" + lastQIndex);
                lastQIndex++;
            }
        }
        return res;
    }

    public static List<String> getQueryParamsBinding(SQLQueryDefinition query) {
        int lastQIndex = 0;
        ArrayList<String> res = new ArrayList<>();
        for (SQLParam param : query.getParams()) {
            if (param.isUnsafe()) continue;
            if (param.isNamed()) {
                res.add(param.getName());
            } else {
                res.add("p" + lastQIndex);
                lastQIndex++;
            }
        }
        return res;
    }

    public static List<String> getUnsafeQueryParamsBinding(SQLQueryDefinition query) {
        return query.getParams().stream()
                .filter(SQLParam::isUnsafe)
                .map(SQLParam::getName)
                .collect(Collectors.toList());
    }

    public static List<Integer> getParamsOffsets(SQLQueryDefinition query) {
        int offset = 0;
        ArrayList<Integer> offsets = new ArrayList<>();
        for (SQLParam param : query.getParams()) {
            offsets.add(param.getStartIndex() - offset);
            offset += param.getNameLength() - 1;
        }
        return offsets;
    }

    private static final String[] searchPackages = {
            "java.lang",
            "java.util",
            "java.math"
    };

    public static String predictType(String type) {
        if (type.contains(".")) return type;
        for (String p : searchPackages) {
            try {
                Class.forName(p + "." + type);
                return p + "." + type;
            } catch (ClassNotFoundException e) {
                //do nothing
            }
        }
        return type;
    }

    public static String methodParams(SQLQueryDefinition queryDefinition) {
        return ParamsUtils.getQueryParamsNames(queryDefinition).stream()
                .map(el -> {
                    String type = queryDefinition.getParamsTypes().get(el);
                    if (type != null) {
                        return ParamsUtils.predictType(type) + " " + el;
                    } else {
                        return "Object " + el;
                    }
                })
                .collect(Collectors.joining(", "));
    }
}
