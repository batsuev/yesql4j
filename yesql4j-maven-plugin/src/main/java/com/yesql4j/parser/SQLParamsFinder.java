package com.yesql4j.parser;

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

    public static List<String> search(String query) {
        State state = State.QUERY;
        ArrayList<String> params = new ArrayList<>();
        StringBuilder namedParam = new StringBuilder();
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
                            namedParam = new StringBuilder();
                            break;
                        case '?':
                            params.add("?");
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
                        params.add(namedParam.toString());
                        namedParam = new StringBuilder();
                        state = State.QUERY;
                    }
                    break;
            }
        }
        if (state == State.PARAMETER) {
            params.add(namedParam.toString());
        }

        return params;
    }
}
