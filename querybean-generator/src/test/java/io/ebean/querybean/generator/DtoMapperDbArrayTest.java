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

import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoMapperDbArrayTest {

  @Test
  void dbArrayList_isSelectedAsScalarColumn() throws IOException {
    Path sourceDir = Files.createTempDirectory("dto-db-array-src");
    Path outDir = Files.createTempDirectory("dto-db-array-out");
    Path genSourceDir = Files.createTempDirectory("dto-db-array-gensrc");

    writeSource(sourceDir, "org.tests.dbarray.ProcessLog",
      "package org.tests.dbarray;\n"
        + "import io.ebean.annotation.DbArray;\n"
        + "import io.ebean.annotation.DbJson;\n"
        + "import io.ebean.annotation.DbJsonB;\n"
        + "import jakarta.persistence.Transient;\n"
        + "public class ProcessLog {\n"
        + "  @DbArray private java.util.List<Long> sourceIds;\n"
        + "  @DbJson private java.util.List<Long> jsonIds;\n"
        + "  @DbJsonB private java.util.List<Long> jsonbIds;\n"
        + "  @Transient private String computed;\n"
        + "  public java.util.List<Long> sourceIds() { return sourceIds; }\n"
        + "  public java.util.List<Long> jsonIds() { return jsonIds; }\n"
        + "  public java.util.List<Long> jsonbIds() { return jsonbIds; }\n"
        + "  public String computed() { return computed; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.dbarray.ProcessLogDto",
      "package org.tests.dbarray;\n"
        + "public class ProcessLogDto {\n"
        + "  private final java.util.List<Long> sourceIds;\n"
        + "  private final java.util.List<Long> jsonIds;\n"
        + "  private final java.util.List<Long> jsonbIds;\n"
        + "  private final String computed;\n"
        + "  public ProcessLogDto(java.util.List<Long> sourceIds, java.util.List<Long> jsonIds,\n"
        + "      java.util.List<Long> jsonbIds, String computed) {\n"
        + "    this.sourceIds = sourceIds; this.jsonIds = jsonIds; this.jsonbIds = jsonbIds; this.computed = computed;\n"
        + "  }\n"
        + "  public java.util.List<Long> getSourceIds() { return sourceIds; }\n"
        + "  public java.util.List<Long> getJsonIds() { return jsonIds; }\n"
        + "  public java.util.List<Long> getJsonbIds() { return jsonbIds; }\n"
        + "  public String getComputed() { return computed; }\n"
        + "}\n");
    writeSource(sourceDir, "org.tests.dbarray.package-info",
      "@io.ebean.annotation.DtoMapping(source = ProcessLog.class, target = ProcessLogDto.class)\n"
        + "package org.tests.dbarray;\n");
    writeSource(sourceDir, "io.ebean.typequery.Generated",
      "package io.ebean.typequery;\n"
        + "public @interface Generated { String value(); }\n");

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null)) {
      List<Path> sourceFiles;
      try (var walk = Files.walk(sourceDir)) {
        sourceFiles = walk.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList());
      }
      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(sourceFiles);
      List<String> options = List.of(
        "-d", outDir.toString(),
        "-s", genSourceDir.toString(),
        "-classpath", System.getProperty("java.class.path"),
        "-processor", Processor.class.getName());

      JavaCompiler.CompilationTask task = compiler.getTask(
        null, fileManager, diagnostics, options, null, compilationUnits);
      assertTrue(task.call(), "compilation failed: " + errors(diagnostics));

      Path mapperFile;
      try (var walk = Files.walk(genSourceDir)) {
        mapperFile = walk.filter(path -> path.getFileName().toString().equals("ProcessLogDtoMapper.java"))
          .findFirst()
          .orElseThrow(() -> new AssertionError("generated mapper source not found"));
      }
      String generated = Files.readString(mapperFile);
      assertTrue(generated.contains("select(\"sourceIds,jsonIds,jsonbIds\")"), generated);
      assertTrue(generated.contains("source.sourceIds()"), generated);
      assertTrue(generated.contains("source.jsonIds()"), generated);
      assertTrue(generated.contains("source.jsonbIds()"), generated);
      assertTrue(generated.contains("source.computed()"), generated);
      assertTrue(!generated.contains("fetch(\"sourceIds\")"), generated);
      assertTrue(!generated.contains("fetch(\"jsonIds\")"), generated);
      assertTrue(!generated.contains("fetch(\"jsonbIds\")"), generated);
    }
  }

  private List<String> errors(DiagnosticCollector<JavaFileObject> diagnostics) {
    return diagnostics.getDiagnostics().stream()
      .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
      .map(diagnostic -> diagnostic.getMessage(Locale.getDefault()))
      .collect(Collectors.toList());
  }

  private void writeSource(Path sourceDir, String fqn, String content) {
    try {
      Path packageDir = sourceDir.resolve(fqn.substring(0, fqn.lastIndexOf('.')).replace('.', '/'));
      Files.createDirectories(packageDir);
      String simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
      try (Writer writer = Files.newBufferedWriter(packageDir.resolve(simpleName + ".java"))) {
        writer.write(content);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
