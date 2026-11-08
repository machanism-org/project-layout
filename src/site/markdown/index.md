<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge [![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/[artifactId]/refs/heads/main/bindex.json)
# Introduction
   - Full description of purpose and benefits.
# Overview
   - Explanation of the project function and value proposition.
   - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
# Key Features
   - Bulleted list highlighting the primary capabilities of the project.
# Getting Started
   - Prerequisites: List of required software and services.
   - Basic Usage: Example command to run the plugin.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Project Layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/project-layout/refs/heads/main/bindex.json)

## Introduction

Project Layout is a Java utility library for describing, detecting, and working with conventional project directory layouts in a consistent way. It gives build tooling, scanners, generators, validation utilities, and plugins a shared model for locating well-known folders such as production sources, test sources, resources, and documentation directories.

Instead of duplicating path conventions throughout each tool, Project Layout centralizes these rules behind reusable layout implementations. This improves maintainability, reduces configuration drift, and makes project-structure discovery easier to adapt across different technology stacks and repository styles.

## Overview

Project Layout provides abstractions and concrete layout strategies for common project ecosystems. `ProjectLayoutManager` detects Maven, Gradle, JavaScript/TypeScript, and Python descriptors in that order, then uses a default filesystem layout when no supported descriptor matches. It configures the selected layout with the project root and reports a missing directory with `FileNotFoundException`.

The shared API exposes source, test, and documentation roots, optional modules, project metadata, relative-path utilities, and exclusion-aware filesystem traversal. Capabilities depend on the layout: Maven derives roots and modules from POM metadata, Gradle exposes conventional roots and discovers child projects through the Tooling API, and JavaScript discovers array-form `package.json` workspaces. Python recognizes named public `pyproject.toml` projects. JavaScript, Python, and default layouts return empty source, test, and documentation collections rather than inferring those directories; the default layout discovers eligible immediate child directories as modules.

The library is especially useful for tools that must operate over heterogeneous repositories or Maven multi-module builds. By resolving important directories through a common API, downstream tools can focus on analysis, generation, documentation, validation, or indexing without hard-coding ecosystem-specific folder rules.

![Project Layout C4 Diagram](./images/c4-diagram.png)

The diagram is generated from the PlantUML source at `src/site/puml/c4-diagram.puml`.

## Key Features

- Common API for source, test, and documentation roots; Maven source and test collections also include resource paths
- Built-in layout implementations for Maven, Gradle, JavaScript/TypeScript, Python, and default project structures
- Project-root-relative path resolution for reliable tool integration
- Maven metadata support through `PomReader` for reading `pom.xml` project information
- Layout coordination through `ProjectLayoutManager` and project processing support through `ProjectProcessor`
- Recursive module traversal with a consumer-defined leaf-project processing hook
- File and directory listing with configurable exclusions
- Suitable for build plugins, repository scanners, code generators, documentation tooling, and validation workflows
- Lightweight Java library designed for reusable integration in other Machai components and external tools

## Getting Started

### Prerequisites

- A JDK compatible with the resolved dependencies and build plugins; the library targets Java 8 bytecode
- Maven 3.x or later for building and consuming the library
- Access to Maven Central or another repository containing `org.machanism.machai:project-layout`
- A project directory whose structure needs to be resolved or analyzed
- For Gradle module discovery, a usable Gradle build and access to any required Gradle distribution or dependency repositories

No hosted service is required for local layout detection. Network access may be needed to resolve build dependencies or a Gradle distribution.

### Basic Usage

Project Layout is a library rather than an executable Maven plugin. Add it to a Maven plugin or other Maven project that needs project-structure resolution:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>1.4.2</version>
</dependency>
```

Resolve a project layout through the common API:

```java
import java.io.File;
import java.io.FileNotFoundException;

import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.ProjectLayout;

File projectDirectory = new File("path/to/project");
try {
  ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDirectory);
  for (String root : layout.getSources()) {
    System.out.println(new File(layout.getProjectDir(), root));
  }
  // getTests() and getDocuments() provide additional root-relative paths.
} catch (FileNotFoundException e) {
  // Handle a project directory that does not exist.
}
```

Project Layout is a library, not a Maven plugin, so it does not provide a Maven goal of its own. Build and verify the library from the project root with Maven; a consuming plugin can then run its own normal Maven goal after adding the dependency:

```bash
mvn clean verify
```

To run a concrete Maven plugin during development, invoke the Maven Compiler
Plugin from the library or consuming project's root:

```bash
mvn org.apache.maven.plugins:maven-compiler-plugin:3.14.0:compile
```

This command compiles Java sources; it does not invoke layout detection. Execute
the Java API from your application or a consuming plugin's implemented goal to
inspect a target repository. There is no `project-layout` plugin goal to run.

### Typical Workflow

1. Add `project-layout` as a dependency to the plugin, scanner, generator, or build tool that needs to inspect project structure.
2. Identify the target project root directory that should be analyzed.
3. Select the appropriate layout implementation, such as `MavenProjectLayout`, `GradleProjectLayout`, `JScriptProjectLayout`, `PythonProjectLayout`, or `DefaultProjectLayout`, or delegate layout coordination to `ProjectLayoutManager`.
4. Read `getSources()`, `getTests()`, and `getDocuments()` and resolve each returned path against `getProjectDir()`. Handle empty collections for layouts that do not infer these roots.
5. Use the resolved paths to drive compilation support, static analysis, code generation, documentation publishing, validation, or project indexing.
6. For module traversal, implement `ProjectProcessor.processFolder(ProjectLayout)` and call `scanFolder(File)` to process detected leaf projects recursively. Reuse the same layout model across tools to keep project-structure handling consistent and maintainable.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/project-layout
- Bindex Metadata: https://raw.githubusercontent.com/machanism-org/project-layout/refs/heads/main/bindex.json
- Machai Project Page: https://machai.machanism.org/project-layout
- Machai Platform: https://machai.machanism.org/
- GitHub Repository: https://github.com/machanism-org/project-layout
- Source Repository: https://github.com/machanism-org/project-layout.git
- Issue Tracker: https://github.com/machanism-org/project-layout/issues
