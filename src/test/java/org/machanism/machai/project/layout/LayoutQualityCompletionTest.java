package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

/**
 * Contract and boundary tests for the layout implementations in this package.
 * Each test deliberately uses local filesystem fixtures so that the tests do
 * not depend on a user's build tools or home directory.
 */
class LayoutQualityCompletionTest {

    @TempDir
    Path tempDir;

    @Test
    void projectLayoutShouldApplyNullAndGlobExclusionsDuringTraversal() throws Exception {
        // Arrange
        DefaultProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("generated", "ignored.txt"));
        Files.createDirectories(tempDir.resolve("src/generated/nested"));
        Files.createDirectories(tempDir.resolve("src/kept"));
        Files.write(tempDir.resolve("src/kept/value.txt"), new byte[] { 1 });
        Files.write(tempDir.resolve("src/ignored.txt"), new byte[] { 1 });

        // Act
        List<File> files = layout.listFiles(tempDir.toFile());

        // Assert
        assertTrue(files.stream().anyMatch(file -> file.getName().equals("value.txt")));
        assertFalse(files.stream().anyMatch(file -> file.getName().equals("ignored.txt")));
        assertFalse(files.stream().anyMatch(file -> file.getPath().contains("generated")));
        assertFalse(layout.isExcludedPath(null));
        assertTrue(layout.isExcludedPath(tempDir.resolve("src/generated").toFile()));
        assertTrue(layout.isExcludedPath(tempDir.resolve("src/ignored.txt").toFile()));
    }

    @Test
    void projectLayoutShouldReturnNullForOutsideAbsolutePathAndEmptyForInvalidDirectories() {
        // Arrange
        File base = tempDir.toFile();
        File outside = tempDir.getParent() == null
                ? new File("outside-file") : tempDir.getParent().resolve("outside-file").toFile();
        ProjectLayout layout = new DefaultProjectLayout();

        // Act
        String relative = ProjectLayout.getRelativePath(base, outside);
        List<File> files = layout.listFiles(new File(tempDir.toFile(), "missing"));
        List<File> directories = layout.listDirectories(null);

        // Assert
        assertNull(relative);
        assertTrue(files.isEmpty());
        assertTrue(directories.isEmpty());
    }

    @Test
    void javaScriptLayoutShouldExpandWorkspaceGlobsAndCacheTheResult() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("package.json"),
                "{\"name\":\"workspace-root\",\"workspaces\":[\"./packages/*\",\"tools/*\"]}"
                        .getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(tempDir.resolve("packages/one"));
        Files.createDirectories(tempDir.resolve("packages/two"));
        Files.createDirectories(tempDir.resolve("tools/not-a-package"));
        Files.write(tempDir.resolve("packages/one/package.json"), "{}".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("packages/two/package.json"), "{}".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> first = layout.getModules();
        Files.createDirectories(tempDir.resolve("packages/late"));
        List<String> second = layout.getModules();

        // Assert
        assertEquals(Arrays.asList("packages/one", "packages/two"), first.stream().sorted().collect(java.util.stream.Collectors.toList()));
        assertSame(first, second);
        assertEquals("workspace-root", layout.getProjectId());
    }

    @Test
    void mavenLayoutShouldApplyDefaultsAndConvertRelativeAndAbsoluteResources() {
        // Arrange
        Model model = new Model();
        model.setPackaging("jar");
        Build build = new Build();
        Resource relative = new Resource();
        relative.setDirectory("src/main/resources");
        build.addResource(relative);
        Resource absolute = new Resource();
        absolute.setDirectory(tempDir.resolve("extra-resources").toString());
        build.addResource(absolute);
        Resource testResource = new Resource();
        testResource.setDirectory(tempDir.resolve("src/test/resources").toString());
        build.addTestResource(testResource);
        model.setBuild(build);
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        java.util.Set<String> sources = layout.getSources();
        List<String> tests = layout.getTests();

        // Assert
        assertTrue(sources.contains("src/main/java"));
        assertTrue(sources.contains("src/main/resources"));
        assertTrue(sources.contains("extra-resources"));
        assertEquals(Arrays.asList("src/test/java", "src/test/resources"), tests);
        assertNull(layout.getProjectId());
    }

    @Test
    void pomReaderShouldRejectMissingOrMalformedPoms() throws Exception {
        // Arrange
        PomReader reader = new PomReader();
        Path malformed = tempDir.resolve("pom.xml");
        Files.write(malformed, "not xml".getBytes(StandardCharsets.UTF_8));

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> reader.getProjectModel(tempDir.resolve("missing.xml").toFile()));
        assertThrows(IllegalArgumentException.class, () -> reader.getProjectModel(malformed.toFile()));
    }

    @Test
    void detectionMethodsShouldHandleDescriptorPresenceAndConventionalAccessors() throws Exception {
        // Arrange
        Files.createFile(tempDir.resolve("build.gradle"));
        Files.createFile(tempDir.resolve("package.json"));
        GradleProjectLayout gradle = new GradleProjectLayout();

        // Act
        boolean gradleDetected = GradleProjectLayout.isGradleProject(tempDir.toFile());
        boolean mavenAbsent = MavenProjectLayout.isMavenProject(tempDir.toFile());
        List<String> gradleSources = gradle.getSources();
        List<String> gradleDocs = gradle.getDocuments();
        List<String> gradleTests = gradle.getTests();

        // Assert
        assertTrue(gradleDetected);
        assertFalse(mavenAbsent);
        assertEquals(Collections.singletonList("src/main"), gradleSources);
        assertEquals(Collections.singletonList("src/site"), gradleDocs);
        assertEquals(Collections.singletonList("src/test"), gradleTests);
        assertTrue(JScriptProjectLayout.isPackageJsonPresent(tempDir.toFile()));
    }
}
