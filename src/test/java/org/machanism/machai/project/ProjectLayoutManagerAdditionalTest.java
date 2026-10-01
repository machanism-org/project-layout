package org.machanism.machai.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;

/** Focused tests for descriptor edge cases at the package boundary. */
class ProjectLayoutManagerAdditionalTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void detectProjectLayout_shouldRejectNullBeforeInspectingFilesystem() {
        // Arrange
        File directory = null;

        // Act
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> ProjectLayoutManager.detectProjectLayout(directory));

        // Assert
        assertEquals("projectDir", exception.getMessage());
    }

    @Test
    void detectProjectLayout_shouldUseDefaultLayoutForExistingEmptyFile() throws Exception {
        // Arrange
        Path file = temporaryDirectory.resolve("descriptorless-file");
        Files.createFile(file);

        // Act
        ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(file.toFile());

        // Assert
        assertInstanceOf(DefaultProjectLayout.class, layout);
        assertEquals(file.toFile(), layout.getProjectDir());
    }
}
