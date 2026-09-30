/**
 * Coordinates project-layout detection and recursive processing of project
 * modules.
 *
 * <p>{@link ProjectLayoutManager} is the package's layout-detection entry
 * point. It examines a directory in the following deterministic order:
 * Maven ({@code pom.xml}), Gradle ({@code build.gradle}), JavaScript or
 * TypeScript ({@code package.json}), and Python ({@code pyproject.toml}). An
 * existing directory without a recognized descriptor receives a
 * {@link org.machanism.machai.project.layout.DefaultProjectLayout}; a missing
 * directory causes {@link java.io.FileNotFoundException}.</p>
 *
 * <p>{@link ProjectProcessor} provides the traversal entry point. It detects a
 * layout for the root directory and recursively scans every module returned by
 * {@link org.machanism.machai.project.layout.ProjectLayout#getModules()}.
 * A {@code null} module list represents a leaf layout and invokes the
 * processor's
 * {@link ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 * hook. A non-null empty list represents a parent with no work and does not
 * invoke the hook. Subclasses should therefore make folder processing safe to
 * call once for each discovered leaf project or module.</p>
 *
 * <p>Layout implementations expose root-relative production-source, test, and
 * documentation paths, where supported by the build system. They can also
 * provide module paths and metadata such as project names, identifiers, and
 * parent identifiers. Resolve returned paths against
 * {@link org.machanism.machai.project.layout.ProjectLayout#getProjectDir()}
 * before accessing the filesystem.</p>
 *
 * <h2>Typical usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("path/to/project");
 * org.machanism.machai.project.ProjectProcessor processor = createProcessor();
 * processor.scanFolder(projectDir);
 * </code></pre>
 *
 * <p>Use the
 * {@link org.machanism.machai.project.layout} package for concrete layout
 * implementations and descriptor-specific behavior, path conventions, and
 * metadata limitations.</p>
 *
 * @since 0.0.2
 */
package org.machanism.machai.project;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `>` as `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
