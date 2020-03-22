package com.yesql4j.plugin;

import com.yesql4j.generator.NameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import com.yesql4j.generator.ClassGenerator;
import com.yesql4j.generator.GenerationTarget;
import com.yesql4j.parser.SQLParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

@Mojo(
        name = "generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true
)
public final class Yesql4jCompile extends AbstractMojo {

    private static final String DEFAULT_INCLUDES = "**/*.sql*";

    @Parameter(
            required = true,
            property = "javaOutputDirectory",
            defaultValue = "${project.build.directory}/generated-sources/sql/java"
    )
    private File outputDirectory;

    @Parameter(
            required = true,
            defaultValue = "${basedir}/src/main/sql"
    )
    private File sqlSourceRoot;

    @Parameter(
            required = true,
            property = "generator"
    )
    private GenerationTarget generator;

    private final SQLParser sqlParser = new SQLParser();
    private final ClassGenerator classGenerator = new ClassGenerator();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sqlSourceRoot != null && sqlSourceRoot.exists()) {
            getLog().info(String.format("source: %s", sqlSourceRoot));
            getLog().info(String.format("target: %s", outputDirectory));
            List<File> sqlSources = findSQLSources(sqlSourceRoot);


            if (!outputDirectory.exists()) {
                FileUtils.mkdir(outputDirectory.getAbsolutePath());
            }

            for (File sqlSource : sqlSources) {
                try {
                    generateSQL(sqlSource);
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed to generate sources", e);
                }
            }
        } else {
            getLog().info(String.format("SQL source folder[%s] not exists", sqlSourceRoot));
        }
    }

    private void generateSQL(File sqlSource) throws IOException {
        var content = FileUtils.fileRead(sqlSource);

        var queries = sqlParser.parse(content);
        var generated = classGenerator.generate(sqlSource.toPath(), sqlSourceRoot.toPath(), queries, this.generator);

        var targetPath = NameUtils.javaGeneratedSourcePath(sqlSource.toPath(), sqlSourceRoot.toPath(), outputDirectory.toPath());

        getLog().info(String.format("%s generated", targetPath));

        FileUtils.forceMkdir(targetPath.toFile().getParentFile());
        FileUtils.fileWrite(targetPath.toFile(), generated);
    }

    private List<File> findSQLSources(File directory) throws MojoExecutionException {
        if (!directory.isDirectory())
            throw new MojoExecutionException(format("'%s' is not a folder", directory));
        try {
            return FileUtils.getFiles(directory, DEFAULT_INCLUDES, "");
        } catch (IOException e) {
            throw new MojoExecutionException("SQL files reading", e);
        }
    }
}
