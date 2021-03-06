package com.yesql4j.parser.params;

import java.util.ArrayList;
import java.util.List;

// TODO: we should use grammar here
public final class SQLParamsFinder {

    private enum State {
        QUERY,
        STRING_SINGLE_QUOTE,
        STRING_DOUBLE_QUOTE,
        PARAMETER
    }

    public static List<SQLParam> search(String query) {
        State state = State.QUERY;
        ArrayList<SQLParam> params = new ArrayList<>();
        StringBuilder namedParam = new StringBuilder();
        int paramNameStartedAt = -1;
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            switch (state) {
                case QUERY:
                    switch (c) {
                        case '\'':
                            state = State.STRING_SINGLE_QUOTE;
                            break;
                        case '\"':
                            state = State.STRING_DOUBLE_QUOTE;
                            break;
                        case ':':
                            state = State.PARAMETER;
                            paramNameStartedAt = i;
                            namedParam = new StringBuilder();
                            break;
                        case '?':
                            params.add(SQLParam.create("?", i));
                            break;
                        default:
                            break;
                    }
                    break;
                case STRING_SINGLE_QUOTE:
                    if (c == '\'') state = State.QUERY;
                    break;
                case STRING_DOUBLE_QUOTE:
                    if (c == '\"') state = State.QUERY;
                    break;
                case PARAMETER:
                    if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_')
                        namedParam.append(c);
                    else {
                        params.add(SQLParam.create(namedParam.toString(), paramNameStartedAt));
                        namedParam = new StringBuilder();
                        paramNameStartedAt = -1;
                        state = State.QUERY;
                    }
                    break;
            }
        }
        if (state == State.PARAMETER) {
            params.add(SQLParam.create(namedParam.toString(), paramNameStartedAt));
        }

        return params;
    }
}
