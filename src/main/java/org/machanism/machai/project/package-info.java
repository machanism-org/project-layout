/**
 * Detects project layouts and traverses their module trees.
 *
 * <p>{@link ProjectLayoutManager} is the package's layout-detection entry
 * point. It checks descriptors in this order: Maven ({@code pom.xml}), Gradle
 * ({@code build.gradle}), JavaScript or TypeScript ({@code package.json}), and
 * Python ({@code pyproject.toml}). An existing directory without a recognized
 * descriptor uses
 * {@link org.machanism.machai.project.layout.DefaultProjectLayout}; a missing
 * directory causes {@link java.io.FileNotFoundException}.</p>
 *
 * <p>{@link ProjectProcessor} is the traversal entry point. It detects the
 * root layout and recursively scans each module returned by
 * {@link org.machanism.machai.project.layout.ProjectLayout#getModules()}.
 * A {@code null} module list identifies a leaf and invokes
 * {@link ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}.
 * A non-null empty list identifies a parent with no modules and does not invoke
 * that hook. Implementations should therefore make folder processing safe to
 * invoke once for each discovered leaf.</p>
 *
 * <p>Layouts expose root-relative production-source, test, and documentation
 * paths when supported by the build system. They may also expose module paths
 * and metadata such as names, identifiers, and parent identifiers. Resolve
 * returned paths against
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
 * implementations, descriptor-specific behavior, path conventions, and
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
