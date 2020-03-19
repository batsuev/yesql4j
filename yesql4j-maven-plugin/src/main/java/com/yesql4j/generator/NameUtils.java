package com.yesql4j.generator;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;

public final class NameUtils {

    public static String methodName(String queryName) {
        var cleanuped = StringUtils.removeEnd(
                StringUtils.removeEnd(
                        queryName.replaceAll("[-]+", "_"),
                        "<!"),
                "!");

        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, cleanuped);
    }

    public static String className(Path path, Path sourceRoot) {
        var p = sourceRoot.relativize(path);
        var fileName = StringUtils.removeEnd(p.getFileName().toString(), ".sql")
                .replace("-", "_");
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, fileName);
    }

    public static String packageName(Path path, Path sourceRoot) {
        var p = sourceRoot.relativize(path).getParent();
        if (p == null) return "";
        var res = new ArrayList<String>();
        for (var i = 0; i < p.getNameCount(); i++) {
            res.add(p.getName(i).toString());
        }
        return StringUtils.join(res, ".");
    }

    public static Path javaGeneratedSourcePath(Path sourcePath, Path sourceRoot, Path generatedSourcesRoot) {
        var packagePath = sourceRoot.relativize(sourcePath).getParent();
        if (packagePath == null)
            return generatedSourcesRoot.resolve(className(sourcePath, sourceRoot) + ".java");

        return generatedSourcesRoot
                .resolve(packagePath)
                .resolve(className(sourcePath, sourceRoot) + ".java");
    }
}
