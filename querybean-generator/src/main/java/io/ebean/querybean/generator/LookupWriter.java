package io.ebean.querybean.generator;

import static java.util.function.Predicate.not;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;

/** Write the source code for the factory. */
class LookupWriter {
  private LookupWriter() {}

  private static final String METAINF_SERVICES_LOOKUP =
      "META-INF/services/io.ebean.config.LookupProvider";

  private static final String FILE_STRING =
      "package %s;\n"
          + "\n"
          + "import java.lang.invoke.MethodHandles;\n"
          + "\n"
          + "import io.ebean.config.LookupProvider;\n"
          + "\n"
          + "public class EbeanMethodLookup implements LookupProvider {\n"
          + "\n"
          + "  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();\n"
          + "\n"
          + "  @Override\n"
          + "  public MethodHandles.Lookup provideLookup() {\n"
          + "    return LOOKUP;\n"
          + "  }\n"
          + "}";

  static void write(
      ProcessingContext processingContext,
      Elements util,
      Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv) {

    var module =
        annotations.stream()
            .map(roundEnv::getElementsAnnotatedWith)
            .filter(not(Collection::isEmpty))
            .findAny()
            .map(s -> s.iterator().next())
            .map(util::getModuleOf)
            .orElse(null);

    if (module != null && !module.isUnnamed()) {
      var moduleNameString = module.getQualifiedName().toString();

      var pkg = moduleNameString + ".lookup";
      String fqn = pkg + ".EbeanMethodLookup";
      try {
        var javaFileObject = processingContext.createWriter(fqn);

        var writer = new Append(javaFileObject.openWriter());

        writer.append(FILE_STRING, pkg);
        writer.close();
        writeServicesFile(processingContext, fqn);
      } catch (IOException e) {
        processingContext.logError(null, "Failed to write lookup class " + e.getMessage());
      }
    }
  }

  private static void writeServicesFile(ProcessingContext processingContext, String fqn)
      throws IOException {

    FileObject jfo = processingContext.createMetaInfWriter(METAINF_SERVICES_LOOKUP);
    if (jfo != null) {
      Writer writer = jfo.openWriter();
      writer.write(fqn);
      writer.close();
    }
  }
}
