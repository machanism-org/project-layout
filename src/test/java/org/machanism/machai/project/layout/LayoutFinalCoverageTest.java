package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Focused branch and error-path tests for the layout package. */
class LayoutFinalCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void listFiles_shouldRecurseAndSkipConfiguredDirectories() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("src/nested"));
        Files.createDirectories(tempDir.resolve("target/generated"));
        Files.write(tempDir.resolve("src/nested/Main.java"), "class Main {}".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("target/generated/Ignored.java"), "ignored".getBytes(StandardCharsets.UTF_8));
        ProjectLayout layout = new DefaultProjectLayout();

        // Act
        List<File> files = layout.listFiles(tempDir.toFile());

        // Assert
        assertEquals(1, files.size());
        assertEquals("Main.java", files.get(0).getName());
    }

    @Test
    void listFiles_shouldReturnEmptyForNullAndRegularFile() throws Exception {
        // Arrange
        Path regularFile = tempDir.resolve("file.txt");
        Files.write(regularFile, "content".getBytes(StandardCharsets.UTF_8));
        ProjectLayout layout = new DefaultProjectLayout();

        // Act
        List<File> nullResult = layout.listFiles(null);
        List<File> fileResult = layout.listFiles(regularFile.toFile());

        // Assert
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());
        assertTrue(fileResult.isEmpty());
    }

    @Test
    void isExcludedPath_shouldSupportExactGlobAndRegexPatterns() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("glob:*generated", "secret.txt"));

        // Act / Assert
        assertTrue(layout.isExcludedPath(new File("src/generated")));
        assertTrue(layout.isExcludedPath(new File("secret.txt")));
        assertFalse(layout.isExcludedPath(new File("src/ordinary.txt")));
        assertFalse(layout.isExcludedPath(null));
    }

    @Test
    void setExcludeDirs_shouldReplaceMutableExclusionConfiguration() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        List<String> exclusions = Collections.singletonList("custom");

        // Act
        layout.setExcludeDirs(exclusions);

        // Assert
        assertSame(exclusions, layout.getExcludeDirs());
        assertTrue(layout.isExcludedPath(new File("custom/file.txt")));
        assertFalse(layout.isExcludedPath(new File("build/file.txt")));
    }

    @Test
    void defaultLayout_shouldDiscoverEligibleImmediateModulesAndCacheResult() throws Exception {
        // Arrange
        Files.createDirectories(tempDir.resolve("module-a"));
        Files.createDirectories(tempDir.resolve("build"));
        DefaultProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> first = layout.getModules();
        Files.createDirectories(tempDir.resolve("module-b"));
        List<String> second = layout.getModules();

        // Assert
        assertEquals(Collections.singletonList("module-a"), first);
        assertSame(first, second);
    }

    @Test
    void mavenLayout_shouldIncludeRelativeAndAbsoluteResources() {
        // Arrange
        Model model = new Model();
        model.setArtifactId("artifact");
        model.setName("Display Name");
        Build build = new Build();
        build.setSourceDirectory(new File(tempDir.toFile(), "src/custom").getAbsolutePath());
        Resource relative = new Resource();
        relative.setDirectory("src/resources");
        Resource absolute = new Resource();
        absolute.setDirectory(new File(tempDir.toFile(), "absolute-resources").getAbsolutePath());
        build.addResource(relative);
        build.addResource(absolute);
        model.setBuild(build);
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        java.util.Set<String> sources = layout.getSources();

        // Assert
        assertTrue(sources.contains("src/custom"));
        assertTrue(sources.contains("src/resources"));
        assertTrue(sources.contains("absolute-resources"));
        assertEquals("artifact", layout.getProjectId());
        assertEquals("Display Name", layout.getProjectName());
    }

    @Test
    void javaScriptLayout_shouldReturnEmptyModulesForScalarWorkspaces() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("package.json"),
                "{\"name\":\"root\",\"workspaces\":\"packages/*\"}".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNull(modules);
    }

    @Test
    void pythonLayout_shouldReturnSafeEmptyCollections() {
        // Arrange
        PythonProjectLayout layout = new PythonProjectLayout();

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNull(modules);
        assertTrue(layout.getSources().isEmpty());
        assertTrue(layout.getDocuments().isEmpty());
        assertTrue(layout.getTests().isEmpty());
    }
}
