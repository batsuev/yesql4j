package com.yesql4j.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class InParameters {

    public static boolean hasListParam(List<Object> params) {
        return params.stream().anyMatch(p -> (p instanceof Collection));
    }

    public static String addListParams(String query, List<Integer> paramsIndexes, List<Object> params) {
        StringBuilder updatedQuery = new StringBuilder(query);
        int offset = 0;
        for (int i = 0; i < params.size(); i++) {
            Integer index = paramsIndexes.get(i);
            if (params.get(i) instanceof Collection) {
                int size = ((Collection) params.get(i)).size();
                String updated = String.join(",", Collections.nCopies(size, "?"));
                updatedQuery.replace(index + offset, index + offset + 1, updated);
                offset += updated.length() - 1;
            }
        }
        return updatedQuery.toString();
    }

    public static List<Object> flattenParams(List<Object> params) {
        ArrayList<Object> res = new ArrayList<>();
        for (Object paramValue : params) {
            if (paramValue instanceof Collection) {
                ((Collection) paramValue).forEach(res::add);
            } else {
                res.add(paramValue);
            }
        }
        return res;
    }
}
