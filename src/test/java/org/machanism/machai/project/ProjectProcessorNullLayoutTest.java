package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/** Boundary tests for invalid layout results returned by processor subclasses. */
class ProjectProcessorNullLayoutTest {

    @TempDir
    File projectDirectory;

    @Test
    void scanFolder_shouldFailImmediatelyWhenLayoutProviderReturnsNull() {
        // Arrange
        NullLayoutProcessor processor = new NullLayoutProcessor();

        // Act
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> processor.scanFolder(projectDirectory));

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("getModules"));
    }

    @Test
    void scanFolder_shouldPropagateFailureWhenLayoutCannotExposeModules() {
        // Arrange
        RuntimeException expected = new RuntimeException("modules unavailable");
        ProjectProcessor processor = new ProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout layout) throws IOException {
                // Not reached: module discovery fails first.
            }

            @Override
            public ProjectLayout getProjectLayout(File directory) {
                return new ProjectLayout() {
                    @Override
                    public File getProjectDir() {
                        return directory;
                    }

                    @Override
                    public java.util.List<String> getModules() {
                        throw expected;
                    }

                    @Override
                    public java.util.List<String> getSources() {
                        return java.util.Collections.emptyList();
                    }

                    @Override
                    public java.util.List<String> getDocuments() {
                        return java.util.Collections.emptyList();
                    }

                    @Override
                    public java.util.List<String> getTests() {
                        return java.util.Collections.emptyList();
                    }
                };
            }
        };

        // Act
        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> processor.scanFolder(projectDirectory));

        // Assert
        assertSame(expected, actual);
    }

    private static final class NullLayoutProcessor extends ProjectProcessor {
        @Override
        public void processFolder(ProjectLayout layout) {
            // Not reached: scanFolder dereferences the detected layout first.
        }

        @Override
        public ProjectLayout getProjectLayout(File directory) {
            return null;
        }
    }
}
