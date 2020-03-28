package com.yesql4j.parser.params;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public final class SQLParam {

    private final @NotNull String name;
    private final int startIndex;

    private SQLParam(@NotNull String name, int startIndex) {
        this.name = name;
        this.startIndex = startIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getName() {
        return name;
    }

    public @NotNull SQLParam offsetLeft(int offset) {
        return new SQLParam(name, startIndex - offset);
    }

    public int getNameLength() {
        return name.length() + 1;
    }

    public int getEndIndex() {
        return startIndex + getNameLength();
    }

    public boolean isNamed() {
        return !name.equals("?");
    }

    @Override
    public String toString() {
        return "SQLParam{" +
                "name='" + name + '\'' +
                ", startIndex=" + startIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLParam sqlParam = (SQLParam) o;
        return startIndex == sqlParam.startIndex &&
                Objects.equals(name, sqlParam.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startIndex);
    }

    public static SQLParam create(@NotNull String name, int startedA) {
        return new SQLParam(name, startedA);
    }
}
