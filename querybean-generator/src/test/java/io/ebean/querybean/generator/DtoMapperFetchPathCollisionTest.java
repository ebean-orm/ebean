package io.ebean.querybean.generator;

import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the fetch-path collision fail-fast added to {@code DtoMapperWriter} - a
 * {@code @DtoPath} property whose fetch path is identical to a {@code NESTED_ONE}/{@code NESTED_MANY}
 * property's own fetch path (e.g. both resolve to path {@code "bar"}) must be a clear compile-time
 * error, not silently generated code where one {@code .fetch("bar", ...)} call discards the other
 * (see {@code OrmQueryDetail.fetch()}, which replaces rather than merges same-key entries).
 * <p>
 * Compiles a minimal in-memory source set directly through {@code javax.tools.JavaCompiler} with
 * this module's {@link Processor} registered explicitly - no external compile-testing dependency
 * required.
 */
class DtoMapperFetchPathCollisionTest {

  @Test
  void dtoPathCollidingWithNestedFetchPath_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-collision-src");
    Path outDir = Files.createTempDirectory("dto-collision-out");
    writeSource(sourceDir, "org.tests.collision.Bar",
      "package org.tests.collision;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "  private String name;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.collision.Foo",
      "package org.tests.collision;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private Bar bar;\n"
        + "\n"
        + "  public Bar getBar() { return bar; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.collision.BarDto",
      "package org.tests.collision;\n"
        + "\n"
        + "public class BarDto {\n"
        + "  private final Long id;\n"
        + "  private final String name;\n"
        + "\n"
        + "  public BarDto(Long id, String name) {\n"
        + "    this.id = id;\n"
        + "    this.name = name;\n"
        + "  }\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.collision.FooDto",
      "package org.tests.collision;\n"
        + "\n"
        + "import io.ebean.annotation.DtoPath;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  private final BarDto bar;\n"
        + "\n"
        + "  @DtoPath(\"bar.name\")\n"
        + "  private final String barName;\n"
        + "\n"
        + "  public FooDto(BarDto bar, String barName) {\n"
        + "    this.bar = bar;\n"
        + "    this.barName = barName;\n"
        + "  }\n"
        + "\n"
        + "  public BarDto getBar() { return bar; }\n"
        + "  public String getBarName() { return barName; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.collision.package-info",
      "@DtoMapping(source = Bar.class, target = BarDto.class)\n"
        + "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.collision;\n"
        + "\n"
        + "import io.ebean.annotation.DtoMapping;\n");

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null)) {
      List<Path> sourceFiles;
      try (var walk = Files.walk(sourceDir)) {
        sourceFiles = walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
      }
      Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjectsFromPaths(sourceFiles);

      List<String> options = List.of(
        "-d", outDir.toString(),
        "-classpath", System.getProperty("java.class.path"),
        "-processor", Processor.class.getName());

      JavaCompiler.CompilationTask task = compiler.getTask(
        null, fileManager, diagnostics, options, null, compilationUnits);
      boolean success = task.call();

      assertFalse(success, "compilation should fail due to the fetch path collision");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("resolves to fetch path 'bar'")
          && msg.contains("collides with the nested mapping"));
      assertTrue(matched, "expected the fetch-path-collision error message, got: " + errors);
    }
  }

  private void writeSource(Path sourceDir, String fqn, String content) {
    try {
      Path pkgDir = sourceDir.resolve(fqn.substring(0, fqn.lastIndexOf('.')).replace('.', '/'));
      Files.createDirectories(pkgDir);
      String simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
      Path file = pkgDir.resolve(simpleName + ".java");
      try (Writer writer = Files.newBufferedWriter(file)) {
        writer.write(content);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
