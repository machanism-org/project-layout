package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/** Additional contract tests for the package-level processor orchestration. */
class ProjectProcessorContractTest {

    @TempDir
    Path tempDirectory;

    @Test
    void scanFolder_shouldPropagateCheckedFailureFromLeafHandler() {
        // Arrange
        IOException expected = new IOException("cannot process leaf");
        ProjectProcessor processor = new ProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout layout) throws IOException {
                throw expected;
            }

            @Override
            public ProjectLayout getProjectLayout(File directory) {
                return new NullModulesLayout();
            }
        };

        // Act
        IOException actual = assertThrows(IOException.class,
                () -> processor.scanFolder(tempDirectory.toFile()));

        // Assert
        assertSame(expected, actual);
    }

    @Test
    void scanFolder_shouldNotCallLeafHandlerForAnEmptyModuleCollection() throws IOException {
        // Arrange
        CountingProcessor processor = new CountingProcessor();

        // Act
        processor.scanFolder(tempDirectory.toFile());

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(0, processor.leafCalls);
    }

    private static final class CountingProcessor extends ProjectProcessor {
        private int leafCalls;

        @Override
        public void processFolder(ProjectLayout layout) {
            leafCalls++;
        }

        @Override
        public ProjectLayout getProjectLayout(File directory) {
            return new ProjectLayout() {
                @Override
                public File getProjectDir() { return directory; }
                @Override
                public java.util.List<String> getModules() { return Collections.emptyList(); }
                @Override
                public java.util.List<String> getSources() { return Collections.emptyList(); }
                @Override
                public java.util.List<String> getDocuments() { return Collections.emptyList(); }
                @Override
                public java.util.List<String> getTests() { return Collections.emptyList(); }
            };
        }
    }

    private static final class NullModulesLayout extends ProjectLayout {
        @Override
        public File getProjectDir() { return null; }
        @Override
        public java.util.List<String> getModules() { return null; }
        @Override
        public java.util.List<String> getSources() { return Collections.emptyList(); }
        @Override
        public java.util.List<String> getDocuments() { return Collections.emptyList(); }
        @Override
        public java.util.List<String> getTests() { return Collections.emptyList(); }
    }
}
