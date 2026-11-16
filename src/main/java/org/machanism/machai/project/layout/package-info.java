/**
 * Detects project types and describes their conventional on-disk layouts.
 *
 * <p>
 * The central contract, {@link org.machanism.machai.project.layout.ProjectLayout}, exposes source, test, and
 * documentation locations together with available module and project-identity metadata. Configure an implementation
 * with its project root using {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)} before
 * calling accessors that inspect project files. Location strings are generally relative to that root; resolve them
 * against the configured directory before filesystem access. The locations describe conventions and, except where
 * noted for exported Access source, do not guarantee that the paths exist.
 * </p>
 *
 * <h2>Layout selection</h2>
 * <p>
 * The descriptor checks provide a way to choose a specialized implementation:
 * {@link org.machanism.machai.project.layout.MavenProjectLayout#isMavenProject(java.io.File)} checks for
 * {@code pom.xml}; {@link org.machanism.machai.project.layout.GradleProjectLayout#isGradleProject(java.io.File)} checks
 * for {@code build.gradle}; and
 * {@link org.machanism.machai.project.layout.JScriptProjectLayout#isPackageJsonPresent(java.io.File)} checks for
 * {@code package.json}. {@link org.machanism.machai.project.layout.PythonProjectLayout#isPythonProject(java.io.File)}
 * additionally validates that {@code pyproject.toml} describes a named, non-private project.
 * {@link org.machanism.machai.project.layout.MSAccessProjectLayout#isAccessProject(java.io.File)} recognizes an
 * msaccess-vcs-addin export by the presence of {@code vcs-options.json} in the root or an immediate child directory.
 * Use {@link org.machanism.machai.project.layout.DefaultProjectLayout} as a generic fallback when no specialized
 * layout applies; it does not infer source, test, documentation, or module locations.
 * </p>
 *
 * <h2>Supported layouts</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} reads the Maven model. For projects with
 *       {@code packaging=pom}, it exposes declared modules; it also reports configured or conventional source and test
 *       roots, resources, the conventional {@code src/site} documentation location, and available project and parent
 *       identifiers.</li>
 *   <li>{@link org.machanism.machai.project.layout.GradleProjectLayout} uses the Gradle Tooling API for the project
 *       model and direct child names. It reports conventional {@code src/main}, {@code src/test}, and {@code src/site}
 *       locations and does not inspect custom source sets.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} reads array-form {@code workspaces} globs
 *       from {@code package.json}; matching directories are reported only when they contain their own
 *       {@code package.json}. It does not infer source, test, or documentation locations.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} recognizes a named, non-private
 *       {@code pyproject.toml} project but does not currently infer module, source, test, or documentation locations.</li>
 *   <li>{@link org.machanism.machai.project.layout.MSAccessProjectLayout} reports the detected
 *       msaccess-vcs-addin export directory as a source location; it does not define test or documentation locations.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} supplies empty source, test, and documentation
 *       collections without interpreting project metadata.</li>
 * </ul>
 *
 * <p>
 * {@link org.machanism.machai.project.layout.ProjectLayout} also provides shared path conversion, directory scanning,
 * exclusion-pattern, project identity, and temporary-directory operations. Maven model parsing and serialization are
 * provided by {@link org.machanism.machai.project.layout.PomReader}.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("repo");
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);
 * java.util.Collection&lt;String&gt; sources = layout.getSources();
 * java.util.Collection&lt;String&gt; tests = layout.getTests();
 * </code></pre>
 *
 * <p>
 * Some metadata accessors read descriptors when invoked and may report invalid or unreadable input according to the
 * implementation. A {@code null} module list may indicate that modules are not declared; callers should handle it
 * rather than assuming every layout returns an empty list.
 * </p>
 */
package org.machanism.machai.project.layout;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      	- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&amp;gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
