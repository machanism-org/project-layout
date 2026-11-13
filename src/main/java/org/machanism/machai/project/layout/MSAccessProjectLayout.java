package org.machanism.machai.project.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects MS Access projects managed with the
 * <a href="https://github.com/joyfullservice/msaccess-vcs-addin">
 * msaccess-vcs-addin</a> (Version Control System add-in for Microsoft Access).
 *
 * <p>
 * The add-in exports an Access database (for example {@code MyDb.accdb}) into a
 * source folder, by default {@code MyDb.accdb.src}, which contains the
 * {@code vcs-options.json} configuration file. {@link #isAccessProject(File)}
 * accepts a directory when {@code vcs-options.json} is found either directly in
 * it or in one of its immediate subdirectories.
 * </p>
 *
 * <p>
 * Exported database objects (VBA modules, forms, reports, queries, macros,
 * table definitions) are reported as source directories. The add-in does not
 * define documentation or test directories, so those accessors return empty
 * lists.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.4.3
 */
public class MSAccessProjectLayout extends ProjectLayout {

	/** Configuration file created by msaccess-vcs-addin in the export folder. */
	private static final String VCS_OPTIONS_FILE_NAME = "vcs-options.json";

	/**
	 * Creates an MS Access project layout instance.
	 */
	public MSAccessProjectLayout() {
		List<String> excludeDirs = getExcludeDirs();
		excludeDirs.add(".git");
		excludeDirs.add(".svn");
		excludeDirs.add(".idea");
		excludeDirs.add(".vscode");
	}

	/**
	 * Checks whether a directory contains an MS Access project exported by
	 * msaccess-vcs-addin.
	 *
	 * <p>
	 * The directory qualifies when it contains {@code vcs-options.json} itself or
	 * when one of its immediate subdirectories (such as {@code MyDb.accdb.src} or
	 * {@code source}) does.
	 * </p>
	 *
	 * @param projectDir directory to examine
	 * @return {@code true} when an msaccess-vcs-addin export folder is detected;
	 *         {@code false} otherwise
	 */
	public static boolean isAccessProject(File projectDir) {
		return findSourceDir(projectDir) != null;
	}

	/**
	 * Locates the msaccess-vcs-addin export folder for a project directory.
	 *
	 * @param projectDir project directory to examine
	 * @return the folder containing {@code vcs-options.json}, or {@code null} if
	 *         none is found
	 */
	private static File findSourceDir(File projectDir) {
		if (projectDir == null || !projectDir.isDirectory()) {
			return null;
		}

		if (new File(projectDir, VCS_OPTIONS_FILE_NAME).isFile()) {
			return projectDir;
		}

		File[] children = projectDir.listFiles(File::isDirectory);
		if (children != null) {
			for (File child : children) {
				if (new File(child, VCS_OPTIONS_FILE_NAME).isFile()) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Returns source directories exported by msaccess-vcs-addin.
	 *
	 * <p>
	 * Paths are relative to the project directory and only existing folders
	 * (modules, forms, reports, queries, macros, tables, table definitions,
	 * relations) are included.
	 * </p>
	 *
	 * @return list of existing export subdirectories; empty if none are found
	 */
	@Override
	public List<String> getSources() {
		File projectDir = getProjectDir();
		File sourceDir = findSourceDir(projectDir);
		if (sourceDir == null) {
			return Collections.emptyList();
		}

		String prefix = sourceDir.equals(projectDir) ? "" : sourceDir.getName();
		List<String> sources = new ArrayList<>();
		sources.add(prefix);
		return sources;
	}

	/**
	 * Returns documentation directories for the Access project.
	 *
	 * @return empty list; msaccess-vcs-addin does not define a documentation folder
	 */
	@Override
	public List<String> getDocuments() {
		return Collections.emptyList();
	}

	/**
	 * Returns test source directories for the Access project.
	 *
	 * @return empty list; msaccess-vcs-addin does not define a test folder
	 */
	@Override
	public List<String> getTests() {
		return Collections.emptyList();
	}

}