package yesql4j.generator;

import org.apache.commons.text.StringEscapeUtils;

public final class EscapeUtils {

    public static String escapedQuery(String query) {
        return StringEscapeUtils.escapeJava(query);
    }
}
