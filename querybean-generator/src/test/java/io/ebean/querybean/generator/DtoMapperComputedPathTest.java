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

  /**
   * Same defect as above, but exercising the {@code NESTED_ONE} branch rather than {@code SCALAR}
   * - a single-hop {@code @DtoPath} rename whose target field type matches a separately registered
   * nested DTO mapping. Prior to the fix, this case bypassed the computed-segment validation
   * entirely (the nested-lookup branch returned early before it ran).
   */
  @Test
  void dtoPathThroughComputedGetter_targetingNestedDto_withoutRequires_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-computed-nested-src");
    Path outDir = Files.createTempDirectory("dto-computed-nested-out");
    writeSource(sourceDir, "org.tests.computednested.Bar",
      "package org.tests.computednested;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "  private String name;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.computednested.BarDto",
      "package org.tests.computednested;\n"
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
    writeSource(sourceDir, "org.tests.computednested.Foo",
      "package org.tests.computednested;\n"
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
    writeSource(sourceDir, "org.tests.computednested.FooDto",
      "package org.tests.computednested;\n"
        + "\n"
        + "import io.ebean.annotation.DtoPath;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoPath(\"firstBar\")\n"
        + "  private final BarDto firstBar;\n"
        + "\n"
        + "  public FooDto(BarDto firstBar) {\n"
        + "    this.firstBar = firstBar;\n"
        + "  }\n"
        + "\n"
        + "  public BarDto getFirstBar() { return firstBar; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.computednested.package-info",
      "@DtoMapping(source = Bar.class, target = BarDto.class)\n"
        + "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.computednested;\n"
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

  /**
   * Same defect class as the {@code @DtoPath} cases above, but for {@code @DtoRef} - a computed
   * association getter with no backing field used via {@code @DtoRef} without {@code requires()}
   * must fail fast at compile time rather than compile cleanly and fail later at runtime.
   */
  @Test
  void dtoRefThroughComputedGetter_withoutRequires_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-ref-computed-src");
    Path outDir = Files.createTempDirectory("dto-ref-computed-out");
    writeSource(sourceDir, "org.tests.refcomputed.Bar",
      "package org.tests.refcomputed;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.refcomputed.Foo",
      "package org.tests.refcomputed;\n"
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
    writeSource(sourceDir, "org.tests.refcomputed.FooDto",
      "package org.tests.refcomputed;\n"
        + "\n"
        + "import io.ebean.annotation.DtoRef;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoRef\n"
        + "  private final Long firstBarId;\n"
        + "\n"
        + "  public FooDto(Long firstBarId) {\n"
        + "    this.firstBarId = firstBarId;\n"
        + "  }\n"
        + "\n"
        + "  public Long getFirstBarId() { return firstBarId; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.refcomputed.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.refcomputed;\n"
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

      assertFalse(success, "compilation should fail due to the missing @DtoRef(requires = ...)");
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

  /**
   * A {@code requires()} path value with a typo'd segment (not the computed segment itself, the
   * developer-declared dependency path) must also fail fast at compile time - {@code requires()}
   * values are handed straight through to {@code FetchGroup.fetch(...)}, so an unchecked typo
   * there would silently reintroduce the exact runtime {@code PersistenceException} the whole
   * escape hatch exists to prevent.
   */
  @Test
  void dtoPathRequires_withTypoInPathValue_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-requires-typo-src");
    Path outDir = Files.createTempDirectory("dto-requires-typo-out");
    writeSource(sourceDir, "org.tests.requirestypo.Bar",
      "package org.tests.requirestypo;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "  private String name;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.requirestypo.Foo",
      "package org.tests.requirestypo;\n"
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
    writeSource(sourceDir, "org.tests.requirestypo.FooDto",
      "package org.tests.requirestypo;\n"
        + "\n"
        + "import io.ebean.annotation.DtoPath;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  // 'barz' is a typo for the real 'bars' property\n"
        + "  @DtoPath(value = \"firstBar.name\", requires = \"barz\")\n"
        + "  private final String firstBarName;\n"
        + "\n"
        + "  public FooDto(String firstBarName) {\n"
        + "    this.firstBarName = firstBarName;\n"
        + "  }\n"
        + "\n"
        + "  public String getFirstBarName() { return firstBarName; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.requirestypo.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.requirestypo;\n"
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

      assertFalse(success, "compilation should fail due to the typo'd requires() path value");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("no backing field")
          && msg.contains("barz")
          && msg.contains("typo"));
      assertTrue(matched, "expected the requires() typo error message, got: " + errors);
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
