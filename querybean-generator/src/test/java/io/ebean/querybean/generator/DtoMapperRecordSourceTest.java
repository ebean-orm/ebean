package io.ebean.querybean.generator;

import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test proving DTO mapping resolves the correct source accessor for two non-JavaBean
 * accessor shapes - both a genuine Java {@code record} source (see {@code
 * org.example.records.CourseRecordEntity} in {@code test-java16}) and, separately, an ordinary
 * (non-record) class that simply exposes bare/fluent-style accessors (e.g. {@code active()},
 * {@code name()}) with no {@code get}/{@code is} prefix at all - Ebean supports record entity
 * beans, but a bare-name accessor isn't exclusive to an actual {@code record} type. Both the
 * accessor call generated for reading the source value, and the Ebean bean-property name used in
 * the generated {@code FetchGroup.select(...)}/{@code .fetch(...)} calls, must match whichever
 * shape the source type actually exposes, rather than blindly guessing a JavaBean {@code get}/
 * {@code is} prefix.
 */
class DtoMapperRecordSourceTest {

  @Test
  void recordSource_expectBareAccessorsAndPropertyNames() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-record-src");
    Path outDir = Files.createTempDirectory("dto-record-out");
    Path genSourceDir = Files.createTempDirectory("dto-record-gensrc");

    writeSource(sourceDir, "org.tests.recordsrc.Region",
      "package org.tests.recordsrc;\n"
        + "\n"
        + "public record Region(long id, String code) {\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.recordsrc.ItemRecord",
      "package org.tests.recordsrc;\n"
        + "\n"
        + "public record ItemRecord(long id, String name, boolean active, Region region) {\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.recordsrc.ItemDto",
      "package org.tests.recordsrc;\n"
        + "\n"
        + "import io.ebean.annotation.DtoRef;\n"
        + "\n"
        + "public class ItemDto {\n"
        + "  private final long id;\n"
        + "  private final String name;\n"
        + "  private final boolean active;\n"
        + "  @DtoRef\n"
        + "  private final Long regionId;\n"
        + "\n"
        + "  public ItemDto(long id, String name, boolean active, Long regionId) {\n"
        + "    this.id = id;\n"
        + "    this.name = name;\n"
        + "    this.active = active;\n"
        + "    this.regionId = regionId;\n"
        + "  }\n"
        + "\n"
        + "  public long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "  public boolean isActive() { return active; }\n"
        + "  public Long getRegionId() { return regionId; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.recordsrc.package-info",
      "@DtoMapping(source = ItemRecord.class, target = ItemDto.class)\n"
        + "package org.tests.recordsrc;\n"
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
        "-s", genSourceDir.toString(),
        "-classpath", classpathWithEbeanApiAndQuerybean(),
        "-processor", Processor.class.getName());

      JavaCompiler.CompilationTask task = compiler.getTask(
        null, fileManager, diagnostics, options, null, compilationUnits);
      boolean success = task.call();

      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertTrue(success, "compilation should succeed for a record source type, got errors: " + errors);

      Path mapperFile;
      try (var walk = Files.walk(genSourceDir)) {
        mapperFile =         walk.filter(p -> p.getFileName().toString().equals("ItemDtoMapper.java"))
          .findFirst()
          .orElseThrow(() -> new AssertionError("generated mapper source not found under " + genSourceDir));
      }
      String generated = Files.readString(mapperFile);

      assertTrue(generated.contains("source.name()"), "expected bare record accessor source.name(), got:\n" + generated);
      assertTrue(generated.contains("source.active()"), "expected bare record accessor source.active(), got:\n" + generated);
      assertFalse(generated.contains("getName()"), "should not guess a JavaBean getter for a record source, got:\n" + generated);
      assertFalse(generated.contains("isActive()"), "should not guess an isXxx() getter for a record source, got:\n" + generated);
      assertFalse(generated.contains("getActive()"), "should not guess a getXxx() getter for a record source, got:\n" + generated);

      assertTrue(generated.contains("select(\"id,name,active,region\")"),
        "expected FetchGroup.select(...) to use bare Ebean property names, got:\n" + generated);

      assertTrue(generated.contains("source.region() == null ? null : source.region().id()"),
        "expected @DtoRef id shortcut to use the associated record's bare id() accessor, got:\n" + generated);
    }
  }

  @Test
  void plainClassWithBareAccessors_expectBareAccessorsResolved() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-bare-src");
    Path outDir = Files.createTempDirectory("dto-bare-out");
    Path genSourceDir = Files.createTempDirectory("dto-bare-gensrc");

    writeSource(sourceDir, "org.tests.baresrc.WidgetSource",
      "package org.tests.baresrc;\n"
        + "\n"
        + "public class WidgetSource {\n"
        + "  private final long id;\n"
        + "  private final String name;\n"
        + "  private final boolean active;\n"
        + "\n"
        + "  public WidgetSource(long id, String name, boolean active) {\n"
        + "    this.id = id;\n"
        + "    this.name = name;\n"
        + "    this.active = active;\n"
        + "  }\n"
        + "\n"
        + "  public long id() { return id; }\n"
        + "  public String name() { return name; }\n"
        + "  public boolean active() { return active; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.baresrc.WidgetDto",
      "package org.tests.baresrc;\n"
        + "\n"
        + "public class WidgetDto {\n"
        + "  private final long id;\n"
        + "  private final String name;\n"
        + "  private final boolean active;\n"
        + "\n"
        + "  public WidgetDto(long id, String name, boolean active) {\n"
        + "    this.id = id;\n"
        + "    this.name = name;\n"
        + "    this.active = active;\n"
        + "  }\n"
        + "\n"
        + "  public long getId() { return id; }\n"
        + "  public String getName() { return name; }\n"
        + "  public boolean isActive() { return active; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.baresrc.package-info",
      "@DtoMapping(source = WidgetSource.class, target = WidgetDto.class)\n"
        + "package org.tests.baresrc;\n"
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
        "-s", genSourceDir.toString(),
        "-classpath", classpathWithEbeanApiAndQuerybean(),
        "-processor", Processor.class.getName());

      JavaCompiler.CompilationTask task = compiler.getTask(
        null, fileManager, diagnostics, options, null, compilationUnits);
      boolean success = task.call();

      List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
        .collect(Collectors.toList());
      assertTrue(success, "compilation should succeed for a plain class with bare accessors, got errors: " + errors);

      Path mapperFile;
      try (var walk = Files.walk(genSourceDir)) {
        mapperFile = walk.filter(p -> p.getFileName().toString().equals("WidgetDtoMapper.java"))
          .findFirst()
          .orElseThrow(() -> new AssertionError("generated mapper source not found under " + genSourceDir));
      }
      String generated = Files.readString(mapperFile);

      assertTrue(generated.contains("source.id()"), "expected bare accessor source.id(), got:\n" + generated);
      assertTrue(generated.contains("source.name()"), "expected bare accessor source.name(), got:\n" + generated);
      assertTrue(generated.contains("source.active()"), "expected bare accessor source.active(), got:\n" + generated);
      assertFalse(generated.contains("getName()"), "should not guess a JavaBean getter, got:\n" + generated);
      assertFalse(generated.contains("isActive()"), "should not guess an isXxx() getter, got:\n" + generated);
      assertFalse(generated.contains("getActive()"), "should not guess a getXxx() getter, got:\n" + generated);

      assertTrue(generated.contains("select(\"id,name,active\")"),
        "expected FetchGroup.select(...) to use bare Ebean property names, got:\n" + generated);
    }
  }

  /**
   * The generated mapper source references {@code io.ebean.DtoMapper}/{@code FetchGroup}/etc.
   * ({@code ebean-api}) and {@code io.ebean.typequery.Generated} ({@code ebean-querybean}) -
   * neither is a compile dependency of {@code querybean-generator} itself (it only needs to
   * process annotations, not compile against the runtime API it emits references to, and adding
   * either as a real dependency here would create a circular reactor module dependency since
   * {@code ebean-querybean} depends on {@code querybean-generator}). Locate their already-built
   * {@code target/classes} directories on disk (built earlier in the same reactor) and append
   * them to the classpath just for this in-process compile, purely to let the generated mapper
   * source fully resolve for this assertion - not needed by the generator itself.
   */
  private String classpathWithEbeanApiAndQuerybean() {
    Path reactorRoot = Path.of("").toAbsolutePath().getParent();
    String extra = Stream.of("ebean-api", "ebean-querybean")
      .map(module -> reactorRoot.resolve(module).resolve("target/classes"))
      .filter(Files::isDirectory)
      .map(Path::toString)
      .collect(Collectors.joining(File.pathSeparator));
    String base = System.getProperty("java.class.path");
    return extra.isEmpty() ? base : base + File.pathSeparator + extra;
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
