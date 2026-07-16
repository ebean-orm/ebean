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
 * Regression test for {@code @DtoPath#requires()} - a {@code @DtoPath} traversing a segment with
 * no backing field (a computed/derived getter, not a real fetchable Ebean property) must fail
 * fast at compile time when {@code requires()} isn't specified, rather than compiling cleanly and
 * failing later at runtime because the generated {@code FetchGroup} doesn't fetch whatever the
 * getter itself needs internally (see docs/dto-mapping-design.md, "computed/derived getter"
 * limitation).
 * <p>
 * Compiles a minimal in-memory source set directly through {@code javax.tools.JavaCompiler} with
 * this module's {@link Processor} registered explicitly - no external compile-testing dependency
 * required (mirrors {@link DtoMapperFetchPathCollisionTest}).
 */
class DtoMapperComputedPathTest {

  @Test
  void dtoPathThroughComputedGetter_withoutRequires_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-computed-src");
    Path outDir = Files.createTempDirectory("dto-computed-out");
    writeSource(sourceDir, "org.tests.computed.Bar",
      "package org.tests.computed;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "  private String name;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.computed.Foo",
      "package org.tests.computed;\n"
        + "\n"
        + "import java.util.List;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private List<Bar> bars;\n"
        + "\n"
        + "  public List<Bar> getBars() { return bars; }\n"
        + "\n"
        + "  // computed/derived getter - no backing 'firstBar' field\n"
        + "  public Bar getFirstBar() { return bars.isEmpty() ? null : bars.get(0); }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.computed.FooDto",
      "package org.tests.computed;\n"
        + "\n"
        + "import io.ebean.annotation.DtoPath;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoPath(\"firstBar.name\")\n"
        + "  private final String firstBarName;\n"
        + "\n"
        + "  public FooDto(String firstBarName) {\n"
        + "    this.firstBarName = firstBarName;\n"
        + "  }\n"
        + "\n"
        + "  public String getFirstBarName() { return firstBarName; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.computed.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.computed;\n"
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

      assertFalse(success, "compilation should fail due to the missing @DtoPath(requires = ...)");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("no backing field")
          && msg.contains("computed/derived getter")
          && msg.contains("requires"));
      assertTrue(matched, "expected the computed-getter requires() error message, got: " + errors);
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
