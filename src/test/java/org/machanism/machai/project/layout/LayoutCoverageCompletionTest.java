package org.machanism.machai.project.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional behavioral tests for the layout implementations and their error paths. */
class LayoutCoverageCompletionTest {

    @TempDir
    Path tempDir;

    @Test
    void projectLayout_shouldExposeDefaultsAndNormalizeRelativePaths() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        File root = tempDir.toFile();
        File child = tempDir.resolve("src/Main.java").toFile();

        // Act
        ProjectLayout configured = layout.projectDir(root);
        String relative = ProjectLayout.getRelativePath(root, child, true);

        // Assert
        assertSame(layout, configured);
        assertSame(root, layout.getProjectDir());
        assertNull(layout.getModules());
        assertNull(layout.getProjectName());
        assertNull(layout.getProjectId());
        assertNull(layout.getParentId());
        assertEquals("Default", layout.getProjectLayoutType());
        assertEquals("./src/Main.java", relative);
        assertEquals("Main.java", layout.getRelativePath(tempDir.resolve("src").toString(), child));
    }

    @Test
    void projectLayout_shouldReturnNullForFileOutsideDirectory() {
        // Arrange
        File root = tempDir.resolve("root").toFile();
        File outside = tempDir.resolve("outside.txt").toFile();
        root.mkdirs();

        // Act
        String result = ProjectLayout.getRelativePath(root, outside);

        // Assert
        assertNull(result);
    }

    @Test
    void projectLayout_shouldSupportRegexAndGlobExclusions() {
        // Arrange
        ProjectLayout layout = new DefaultProjectLayout();
        layout.setExcludeDirs(Arrays.asList("tmp", "glob:**/generated/Code.java"));

        // Act / Assert
        assertTrue(layout.isExcludedPath(new File("tmp/notes.txt")));
        assertTrue(layout.isExcludedPath(new File("src/generated/Code.java")));
        assertFalse(layout.isExcludedPath(new File("src/Code.java")));
    }

    @Test
    void defaultLayout_shouldReturnNoModulesForMissingOrEmptyRoot() {
        // Arrange
        DefaultProjectLayout missingRoot = new DefaultProjectLayout();
        DefaultProjectLayout emptyRoot = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> missing = missingRoot.getModules();
        List<String> empty = emptyRoot.getModules();

        // Assert
        assertNull(missing);
        assertNull(empty);
        assertTrue(emptyRoot.getSources().isEmpty());
        assertTrue(emptyRoot.getDocuments().isEmpty());
        assertTrue(emptyRoot.getTests().isEmpty());
    }

    @Test
    void gradleLayout_shouldExposeConventionsAndHandleUnavailableModel() throws Exception {
        // Arrange
        GradleProjectLayout layout = new GradleProjectLayout().projectDir(tempDir.toFile());
        Files.write(tempDir.resolve("build.gradle"), "plugins { }".getBytes(StandardCharsets.UTF_8));

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertTrue(GradleProjectLayout.isGradleProject(tempDir.toFile()));
        assertFalse(GradleProjectLayout.isGradleProject(tempDir.resolve("missing").toFile()));
        assertNull(modules);
        assertEquals(Collections.singletonList("src/main"), layout.getSources());
        assertEquals(Collections.singletonList("src/site"), layout.getDocuments());
        assertEquals(Collections.singletonList("src/test"), layout.getTests());
        assertEquals("", layout.getProjectId());
        assertEquals("", layout.getProjectName());
    }

    @Test
    void mavenLayout_shouldApplyBuildDefaultsAndExposeTestsAndParent() {
        // Arrange
        Model model = new Model();
        model.setArtifactId("child");
        org.apache.maven.model.Parent parent = new org.apache.maven.model.Parent();
        parent.setArtifactId("parent");
        model.setParent(parent);
        model.setBuild(new Build());
        MavenProjectLayout layout = new MavenProjectLayout().projectDir(tempDir.toFile()).model(model);

        // Act
        Set<String> sources = layout.getSources();
        List<String> tests = layout.getTests();

        // Assert
        assertTrue(sources.contains("src/main/java"));
        assertTrue(tests.contains("src/test/java"));
        assertEquals("parent", layout.getParentId());
        assertEquals(Collections.singletonList("src/site"), layout.getDocuments());
        assertNull(layout.getModules());
    }

    @Test
    void mavenLayout_shouldReturnDeclaredModulesOnlyForPomPackaging() {
        // Arrange
        Model model = new Model();
        model.setPackaging("pom");
        model.setModules(Arrays.asList("one", "two"));
        MavenProjectLayout layout = new MavenProjectLayout().model(model);

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertEquals(Arrays.asList("one", "two"), modules);
    }

    @Test
    void javascriptLayout_shouldDiscoverUniqueWorkspacePackagesAndProjectName() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("package.json"),
                "{\"name\":\"workspace-root\",\"workspaces\":[\"./packages/*\",\"packages/a\"]}"
                        .getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(tempDir.resolve("packages/a"));
        Files.createDirectories(tempDir.resolve("packages/b"));
        Files.write(tempDir.resolve("packages/a/package.json"), "{}".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("packages/b/package.json"), "{}".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout layout = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act
        List<String> modules = layout.getModules();

        // Assert
        assertNotNull(modules);
        assertEquals(2, modules.size());
        assertTrue(modules.contains("packages/a"));
        assertTrue(modules.contains("packages/b"));
        assertEquals("workspace-root", layout.getProjectId());
    }

    @Test
    void javascriptLayout_shouldRejectMissingRootAndMalformedDescriptor() throws Exception {
        // Arrange
        JScriptProjectLayout unconfigured = new JScriptProjectLayout();
        Files.write(tempDir.resolve("package.json"), "not-json".getBytes(StandardCharsets.UTF_8));
        JScriptProjectLayout malformed = new JScriptProjectLayout().projectDir(tempDir.toFile());

        // Act / Assert
        assertThrows(IllegalStateException.class, unconfigured::getModules);
        assertThrows(IllegalArgumentException.class, malformed::getProjectId);
    }

    @Test
    void pythonLayout_shouldDetectPublicPrivateAndInvalidProjects() throws Exception {
        // Arrange
        Files.write(tempDir.resolve("pyproject.toml"),
                "[project]\nname = 'public-name'\nclassifiers = ['Topic :: Utility']\n"
                        .getBytes(StandardCharsets.UTF_8));

        // Act
        boolean publicProject = PythonProjectLayout.isPythonProject(tempDir.toFile());
        Files.write(tempDir.resolve("pyproject.toml"),
                "[project]\nname = 'private-name'\nclassifiers = ['Private :: Internal']\n"
                        .getBytes(StandardCharsets.UTF_8));
        boolean privateProject = PythonProjectLayout.isPythonProject(tempDir.toFile());
        Files.write(tempDir.resolve("pyproject.toml"), "bad = [".getBytes(StandardCharsets.UTF_8));
        boolean invalidProject = PythonProjectLayout.isPythonProject(tempDir.toFile());

        // Assert
        assertTrue(publicProject);
        assertFalse(privateProject);
        assertFalse(invalidProject);
        assertFalse(PythonProjectLayout.isPythonProject(tempDir.resolve("missing").toFile()));
    }

    @Test
    void pomReader_shouldReplacePropertiesCarryLicensesAndSerializeModels() throws Exception {
        // Arrange
        Path first = tempDir.resolve("first.xml");
        Files.write(first, ("<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId>"
                + "<artifactId>a</artifactId><version>1.2</version><properties><custom>value</custom></properties>"
                + "<licenses><license><name>Apache</name></license></licenses></project>")
                .getBytes(StandardCharsets.UTF_8));
        Path second = tempDir.resolve("second.xml");
        Files.write(second, ("<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId>"
                + "<artifactId>${custom}</artifactId></project>").getBytes(StandardCharsets.UTF_8));
        PomReader reader = new PomReader();

        // Act
        Model firstModel = reader.getProjectModel(first.toFile());
        Model secondModel = reader.getProjectModel(second.toFile());
        String xml = PomReader.printModel(secondModel);

        // Assert
        assertEquals("a", firstModel.getArtifactId());
        assertEquals("value", secondModel.getArtifactId());
        assertEquals("1.2", reader.getPomProperties().get("project.version"));
        assertEquals(1, secondModel.getLicenses().size());
        assertTrue(xml.contains("<artifactId>value</artifactId>"));
    }

    @Test
    void pomReader_shouldRejectMissingPom() {
        // Arrange
        PomReader reader = new PomReader();

        // Act / Assert
        assertThrows(IllegalArgumentException.class,
                () -> reader.getProjectModel(tempDir.resolve("missing.xml").toFile()));
    }
}
