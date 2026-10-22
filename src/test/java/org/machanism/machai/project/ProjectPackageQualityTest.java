package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Focused package-level contract tests that do not depend on layout
 * implementations in sub-packages.
 */
class ProjectPackageQualityTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void getProjectLayout_shouldRejectNullDirectoryBeforeFilesystemAccess() {
        // Arrange
        ProjectProcessor processor = new RecordingProcessor();

        // Act
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> processor.getProjectLayout(null));

        // Assert
        assertEquals("projectDir", exception.getMessage());
    }

    @Test
    void scanFolder_shouldProcessTheExactLayoutReturnedForAProjectWithNoModules() throws Exception {
        // Arrange
        RecordingProcessor processor = new RecordingProcessor();
        ProjectLayout expectedLayout = new NullModulesLayout(temporaryDirectory.toFile());
        processor.layout = expectedLayout;

        // Act
        processor.scanFolder(temporaryDirectory.toFile());

        // Assert
        assertEquals(1, processor.processedCount);
        assertEquals(expectedLayout, processor.processedLayout);
    }

    @Test
    void scanFolder_shouldStopBeforeProcessingWhenLayoutLookupFails() {
        // Arrange
        File missingDirectory = temporaryDirectory.resolve("missing").toFile();
        ProjectProcessor processor = new ProjectProcessor() {
            @Override
            public void processFolder(ProjectLayout layout) {
                throw new AssertionError("the folder must not be processed");
            }
        };

        // Act
        FileNotFoundException exception = assertThrows(FileNotFoundException.class,
                () -> processor.scanFolder(missingDirectory));

        // Assert
        assertEquals(missingDirectory.getAbsolutePath(), exception.getMessage());
    }

    private static final class RecordingProcessor extends ProjectProcessor {
        private ProjectLayout layout;
        private int processedCount;
        private ProjectLayout processedLayout;

        @Override
        public void processFolder(ProjectLayout layout) throws IOException {
            processedCount++;
            processedLayout = layout;
        }

        @Override
        public ProjectLayout getProjectLayout(File directory) throws FileNotFoundException {
            if (layout != null) {
                return layout;
            }
            return super.getProjectLayout(directory);
        }
    }

    private static final class NullModulesLayout extends ProjectLayout {
        private final File directory;

        private NullModulesLayout(File directory) {
            this.directory = directory;
        }

        @Override
        public File getProjectDir() {
            return directory;
        }

        @Override
        public java.util.List<String> getModules() {
            return null;
        }

        @Override
        public java.util.List<String> getSources() {
            return Collections.emptyList();
        }

        @Override
        public java.util.List<String> getDocuments() {
            return Collections.emptyList();
        }

        @Override
        public java.util.List<String> getTests() {
            return Collections.emptyList();
        }
    }
}
