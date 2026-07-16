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

  /**
   * Two {@code @DtoMixin} companion types targeting the same DTO class must fail fast at compile
   * time - previously the second registration silently overwrote the first in
   * {@code mixinsByTarget}, so whichever mixin was processed last would win with no diagnostic at
   * all, silently discarding the other mixin's {@code @DtoPath}/{@code @DtoRef}/{@code @DtoConvert}
   * overlays.
   */
  @Test
  void duplicateDtoMixin_forSameTarget_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-mixin-dup-src");
    Path outDir = Files.createTempDirectory("dto-mixin-dup-out");
    writeSource(sourceDir, "org.tests.mixindup.FooDto",
      "package org.tests.mixindup;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  private final String bar;\n"
        + "\n"
        + "  public FooDto(String bar) {\n"
        + "    this.bar = bar;\n"
        + "  }\n"
        + "\n"
        + "  public String getBar() { return bar; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.mixindup.FooMixinA",
      "package org.tests.mixindup;\n"
        + "\n"
        + "import io.ebean.annotation.DtoMixin;\n"
        + "\n"
        + "@DtoMixin(FooDto.class)\n"
        + "interface FooMixinA {\n"
        + "  String bar();\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.mixindup.FooMixinB",
      "package org.tests.mixindup;\n"
        + "\n"
        + "import io.ebean.annotation.DtoMixin;\n"
        + "\n"
        + "@DtoMixin(FooDto.class)\n"
        + "interface FooMixinB {\n"
        + "  String bar();\n"
        + "}\n");

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

      assertFalse(success, "compilation should fail due to the duplicate @DtoMixin for the same target");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("Duplicate @DtoMixin") && msg.contains("FooDto"));
      assertTrue(matched, "expected the duplicate @DtoMixin error message, got: " + errors);
    }
  }

  /**
   * A field carrying both {@code @DtoRef} and {@code @DtoPath} at once must fail fast at compile
   * time - previously {@code resolveProperty()} checked {@code @DtoRef} first and returned early,
   * silently ignoring any {@code @DtoPath} also present on the same field with no diagnostic,
   * discarding whichever rename/path semantics the developer actually intended.
   */
  @Test
  void dtoRefAndDtoPath_onSameField_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-ref-path-conflict-src");
    Path outDir = Files.createTempDirectory("dto-ref-path-conflict-out");
    writeSource(sourceDir, "org.tests.refpathconflict.Bar",
      "package org.tests.refpathconflict;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.refpathconflict.Foo",
      "package org.tests.refpathconflict;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private Bar bar;\n"
        + "\n"
        + "  public Bar getBar() { return bar; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.refpathconflict.FooDto",
      "package org.tests.refpathconflict;\n"
        + "\n"
        + "import io.ebean.annotation.DtoPath;\n"
        + "import io.ebean.annotation.DtoRef;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoRef\n"
        + "  @DtoPath(\"bar.id\")\n"
        + "  private final Long barId;\n"
        + "\n"
        + "  public FooDto(Long barId) {\n"
        + "    this.barId = barId;\n"
        + "  }\n"
        + "\n"
        + "  public Long getBarId() { return barId; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.refpathconflict.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.refpathconflict;\n"
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

      assertFalse(success, "compilation should fail due to both @DtoRef and @DtoPath on the same field");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("both @DtoRef and @DtoPath") && msg.contains("mutually exclusive"));
      assertTrue(matched, "expected the @DtoRef/@DtoPath conflict error message, got: " + errors);
    }
  }

  /**
   * {@code @DtoConvert} on a {@code NESTED_ONE} (or {@code NESTED_MANY}) property must fail fast
   * at compile time - previously the converter was resolved but simply never wired into the
   * {@code NESTED_ONE}/{@code NESTED_MANY} {@link DtoPropertyMeta} constructor calls, so the
   * annotation silently had zero effect (the nested mapper's own {@code map(...)} call always
   * fully determines the value), with no diagnostic telling the developer it was ignored.
   */
  @Test
  void dtoConvertOnNestedOne_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-convert-nested-src");
    Path outDir = Files.createTempDirectory("dto-convert-nested-out");
    writeSource(sourceDir, "org.tests.convertnested.Bar",
      "package org.tests.convertnested;\n"
        + "\n"
        + "public class Bar {\n"
        + "  private Long id;\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertnested.BarDto",
      "package org.tests.convertnested;\n"
        + "\n"
        + "public class BarDto {\n"
        + "  private final Long id;\n"
        + "\n"
        + "  public BarDto(Long id) {\n"
        + "    this.id = id;\n"
        + "  }\n"
        + "\n"
        + "  public Long getId() { return id; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertnested.Foo",
      "package org.tests.convertnested;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private Bar bar;\n"
        + "\n"
        + "  public Bar getBar() { return bar; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertnested.BarConverter",
      "package org.tests.convertnested;\n"
        + "\n"
        + "public class BarConverter {\n"
        + "  public static BarDto identity(BarDto dto) { return dto; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertnested.FooDto",
      "package org.tests.convertnested;\n"
        + "\n"
        + "import io.ebean.annotation.DtoConvert;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoConvert(value = BarConverter.class, method = \"identity\")\n"
        + "  private final BarDto bar;\n"
        + "\n"
        + "  public FooDto(BarDto bar) {\n"
        + "    this.bar = bar;\n"
        + "  }\n"
        + "\n"
        + "  public BarDto getBar() { return bar; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertnested.package-info",
      "@DtoMapping(source = Bar.class, target = BarDto.class)\n"
        + "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.convertnested;\n"
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

      assertFalse(success, "compilation should fail due to @DtoConvert on a NESTED_ONE property");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("@DtoConvert") && msg.contains("has no effect")
          && msg.contains("nested DTO graph property"));
      assertTrue(matched, "expected the @DtoConvert-on-nested error message, got: " + errors);
    }
  }

  /**
   * A {@code @DtoConvert(method = ...)} reference to a converter type with two overloads sharing
   * that name, both taking exactly one parameter, must fail fast at compile time rather than
   * silently binding to whichever overload {@code ElementFilter.methodsIn} happens to return
   * first (unrelated to which one the developer actually meant) - {@code @DtoConvert} has no way
   * to disambiguate by parameter type since it's declared by name alone.
   */
  @Test
  void dtoConvertMethod_withAmbiguousOverloads_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-convert-ambiguous-src");
    Path outDir = Files.createTempDirectory("dto-convert-ambiguous-out");
    writeSource(sourceDir, "org.tests.convertambiguous.Foo",
      "package org.tests.convertambiguous;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private String name;\n"
        + "\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertambiguous.NameConverter",
      "package org.tests.convertambiguous;\n"
        + "\n"
        + "public class NameConverter {\n"
        + "  public static String format(String value) { return value; }\n"
        + "  public static String format(Object value) { return String.valueOf(value); }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertambiguous.FooDto",
      "package org.tests.convertambiguous;\n"
        + "\n"
        + "import io.ebean.annotation.DtoConvert;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoConvert(value = NameConverter.class, method = \"format\")\n"
        + "  private final String name;\n"
        + "\n"
        + "  public FooDto(String name) {\n"
        + "    this.name = name;\n"
        + "  }\n"
        + "\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertambiguous.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.convertambiguous;\n"
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

      assertFalse(success, "compilation should fail due to the ambiguous @DtoConvert method overloads");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("ambiguous") && msg.contains("format") && msg.contains("2 overloads"));
      assertTrue(matched, "expected the ambiguous @DtoConvert overload error message, got: " + errors);
    }
  }

  /**
   * A {@code @DtoConvert(method = ...)} reference to a method that exists on the converter type
   * but doesn't take exactly one parameter (e.g. a zero-arg or two-arg overload sharing the name)
   * must fail fast at compile time with a clear message, rather than {@code findMethod} matching
   * it anyway and generating a call the compiler will reject with an unrelated arity-mismatch
   * error in the generated mapper source.
   */
  @Test
  void dtoConvertMethod_withWrongArity_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-convert-arity-src");
    Path outDir = Files.createTempDirectory("dto-convert-arity-out");
    writeSource(sourceDir, "org.tests.convertarity.Foo",
      "package org.tests.convertarity;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private String name;\n"
        + "\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertarity.NameConverter",
      "package org.tests.convertarity;\n"
        + "\n"
        + "public class NameConverter {\n"
        + "  public static String format() { return \"\"; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertarity.FooDto",
      "package org.tests.convertarity;\n"
        + "\n"
        + "import io.ebean.annotation.DtoConvert;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  @DtoConvert(value = NameConverter.class, method = \"format\")\n"
        + "  private final String name;\n"
        + "\n"
        + "  public FooDto(String name) {\n"
        + "    this.name = name;\n"
        + "  }\n"
        + "\n"
        + "  public String getName() { return name; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.convertarity.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "package org.tests.convertarity;\n"
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

      assertFalse(success, "compilation should fail due to the wrong-arity @DtoConvert method");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("not found on") && msg.contains("exactly one parameter"));
      assertTrue(matched, "expected the wrong-arity @DtoConvert error message, got: " + errors);
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

  /**
   * A named variant may exclude a plain non-primitive {@code SCALAR} property (not just
   * {@code NESTED_ONE}/{@code NESTED_MANY}) - see docs/dto-mapping-requirements.md "Section G"
   * follow-up - but a primitive-typed scalar property still can't be excluded (no type-safe
   * "absent" value).
   */
  @Test
  void excludeVariant_onPrimitiveScalarProperty_expectCompileError() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-variant-primitive-src");
    Path outDir = Files.createTempDirectory("dto-variant-primitive-out");
    writeSource(sourceDir, "org.tests.variantprimitive.Foo",
      "package org.tests.variantprimitive;\n"
        + "\n"
        + "public class Foo {\n"
        + "  private String name;\n"
        + "  private int score;\n"
        + "\n"
        + "  public String getName() { return name; }\n"
        + "  public int getScore() { return score; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.variantprimitive.FooDto",
      "package org.tests.variantprimitive;\n"
        + "\n"
        + "public class FooDto {\n"
        + "  private final String name;\n"
        + "  private final int score;\n"
        + "\n"
        + "  public FooDto(String name, int score) {\n"
        + "    this.name = name;\n"
        + "    this.score = score;\n"
        + "  }\n"
        + "\n"
        + "  public String getName() { return name; }\n"
        + "  public int getScore() { return score; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.variantprimitive.package-info",
      "@DtoMapping(source = Foo.class, target = FooDto.class)\n"
        + "@DtoMapping(source = Foo.class, target = FooDto.class, name = \"noScore\", exclude = \"score\")\n"
        + "package org.tests.variantprimitive;\n"
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

      assertFalse(success, "compilation should fail excluding a primitive scalar property from a variant");
      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertFalse(errors.isEmpty(), "expected at least one compile ERROR diagnostic");
      boolean matched = errors.stream()
        .map(d -> d.getMessage(Locale.getDefault()))
        .anyMatch(msg -> msg.contains("primitive property") && msg.contains("can't be"));
      assertTrue(matched, "expected the primitive-scalar-exclusion error message, got: " + errors);
    }
  }
}
