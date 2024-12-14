package io.ebean.querybean.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class QueryBeanProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws Exception {

    Paths.get("ebean-generated-info.mf").toAbsolutePath().toFile().delete();
    Paths.get("io.ebean.config.EntityClassRegister").toAbsolutePath().toFile().delete();
    Paths.get("reflect-config.json").toAbsolutePath().toFile().delete();
    Files.walk(Paths.get("io").toAbsolutePath())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  // uncomment this to debug the processor for diagnosing issues
  // @Test
  void testGeneration() throws Exception {
    final String source =
        Paths.get("src/test/java/io/ebean/querybean/generator/entities")
            .toAbsolutePath()
            .toString();

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

    manager.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Set.of(Kind.SOURCE);

    final Iterable<JavaFileObject> files =
        manager.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=" + Integer.getInteger("java.specification.version")),
            null,
            files);
    task.setProcessors(List.of(new Processor()));

    assertThat(task.call()).isTrue();
  }
}
