package com.yesql4j.parser.params;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public final class SQLParam {

    private final @NotNull String name;
    private final int startIndex;
    private final boolean isUnsafe;

    private SQLParam(@NotNull String name, int startIndex, boolean isUnsafe) {
        this.name = name;
        this.startIndex = startIndex;
        this.isUnsafe = isUnsafe;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getName() {
        return name;
    }

    public @NotNull SQLParam offsetLeft(int offset) {
        return new SQLParam(name, startIndex - offset, isUnsafe);
    }

    public int getNameLength() {
        if (isUnsafe) { // [:name]
            return name.length() + 3;
        }else {         // :name
            return name.length() + 1;
        }
    }

    public int getEndIndex() {
        return startIndex + getNameLength();
    }

    public boolean isNamed() {
        return !name.equals("?");
    }

    public boolean isUnsafe() {
        return isUnsafe;
    }

    @Override
    public String toString() {
        return "SQLParam{" +
                "name='" + name + '\'' +
                ", startIndex=" + startIndex +
                ", isUnsafe=" + isUnsafe +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLParam sqlParam = (SQLParam) o;
        return startIndex == sqlParam.startIndex &&
                isUnsafe == sqlParam.isUnsafe &&
                Objects.equals(name, sqlParam.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startIndex, isUnsafe);
    }

    public static SQLParam create(@NotNull String name, int startedA) {
        return new SQLParam(name, startedA, false);
    }

    public static SQLParam create(@NotNull String name, int startedA, boolean isUnsafe) {
        return new SQLParam(name, startedA, isUnsafe);
    }
}
