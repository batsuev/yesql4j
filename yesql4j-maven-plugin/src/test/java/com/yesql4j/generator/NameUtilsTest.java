package com.yesql4j.generator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameUtilsTest {

    @Test
    void methodName() {
        assertEquals("getUser", NameUtils.methodName("get-user"));
        assertEquals("updateUser", NameUtils.methodName("update-user!"));
        assertEquals("createUser", NameUtils.methodName("create-user<!"));
    }

    @Test
    void className() {
        var rootPath = Path.of("/yesql4j/src/sql");
        var filePath = Path.of("/yesql4j/src/sql/data/combined_file.sql");

        assertEquals("CombinedFile", NameUtils.className(filePath, rootPath));
    }

    @Test
    void packageName() {
        var rootPath = Path.of("/yesql4j/src/sql");
        var filePath = Path.of("/yesql4j/src/sql/data/combined_file.sql");

        assertEquals("data", NameUtils.packageName(filePath, rootPath));
    }
}