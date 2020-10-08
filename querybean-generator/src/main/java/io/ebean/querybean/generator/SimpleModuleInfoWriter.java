package io.ebean.querybean.generator;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Write the source code for the factory.
 */
class SimpleModuleInfoWriter {

  private final ProcessingContext processingContext;

  private final String factoryPackage;
  private final String factoryShortName;
  private final String factoryFullName;

  private Append writer;

  SimpleModuleInfoWriter(ProcessingContext processingContext) {
    this.processingContext = processingContext;
    this.factoryPackage = processingContext.getFactoryPackage();
    this.factoryShortName = "_ebean$ModuleInfo";
    this.factoryFullName = factoryPackage + "." + factoryShortName;
  }

  void write() throws IOException {
    writer = new Append(createFileWriter());
    writePackage();
    writeStartClass();
    writeEndClass();
    writer.close();
    writeServicesFile();
    writeManifestFile();
  }

  private void writeServicesFile() {
    try {
      FileObject jfo = processingContext.createMetaInfServicesWriter();
      if (jfo != null) {
        Writer writer = jfo.openWriter();
        writer.write(factoryFullName);
        writer.close();
      }

    } catch (IOException e) {
      e.printStackTrace();
      processingContext.logError(null, "Failed to write services file " + e.getMessage());
    }
  }

  private void writeManifestFile() {
    try {
      final Set<String> allEntityPackages = processingContext.getAllEntityPackages();
      if (!allEntityPackages.isEmpty()) {
        FileObject jfo = processingContext.createManifestWriter();
        if (jfo != null) {
          Writer writer = jfo.openWriter();
          writer.write("generated-by: Ebean query bean generator\n");
          writer.write(manifestEntityPackages(allEntityPackages));
          writer.write("\n");
          writer.close();
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      processingContext.logError(null, "Failed to write services file " + e.getMessage());
    }
  }

  private String manifestEntityPackages(Set<String> allEntityPackages) {
    StringBuilder builder = new StringBuilder("entity-packages: ");
    for (String pkg : allEntityPackages) {
      // one package per line
      builder.append(pkg).append("\n").append("  ");
    }
    return builder.delete(builder.lastIndexOf("\n"), builder.length()).append("\n").toString();
  }

  private void writePackage() {

    writer.append("package %s;", factoryPackage).eol().eol();

    writer.append("import java.util.ArrayList;").eol();
    writer.append("import java.util.Collections;").eol();
    writer.append("import java.util.List;").eol();
    final String generated = processingContext.getGeneratedAnnotation();
    if (generated != null) {
      writer.append("import %s;", generated).eol();
    }
    writer.eol();
    writer.append("import io.ebean.config.ModuleInfo;").eol();
    writer.append("import io.ebean.config.ModuleInfoLoader;").eol();
    writer.eol();
  }

  void buildAtContextModule(Append writer) {
    if (processingContext.isGeneratedAvailable()) {
      writer.append(Constants.AT_GENERATED).eol();
    }
    writer.append("@ModuleInfo(");
    if (processingContext.hasOtherClasses()) {
      writer.append("other={%s}, ", otherClasses());
    }
    writer.append("entities={%s}", prefixEntities());
    writer.append(")").eol();
  }

  private String otherClasses() {
    return quoteTypes(processingContext.getOtherClasses());
  }

  private String prefixEntities() {
    return quoteTypes(processingContext.getPrefixEntities());
  }

  private String quoteTypes(Set<String> otherClasses) {
    StringJoiner sb = new StringJoiner(",");
    for (String fullType : otherClasses) {
      sb.add("\"" + fullType + "\"");
    }
    return sb.toString();
  }

  private void writeStartClass() {

    buildAtContextModule(writer);

    writer.append("public class %s implements ModuleInfoLoader {", factoryShortName).eol().eol();
    writeMethodOtherClasses();
    writeMethodEntityClasses(processingContext.getDbEntities(), null);

    final Map<String, Set<String>> otherDbEntities = processingContext.getOtherDbEntities();
    writeMethodEntityClassesFor(otherDbEntities.keySet());

    for (Map.Entry<String, Set<String>> otherDb : otherDbEntities.entrySet()) {
      writeMethodEntityClasses(otherDb.getValue(), otherDb.getKey());
    }
  }

  private void writeMethodOtherClasses() {
    writer.append("  private List<Class<?>> otherClasses() {").eol();
    if (!processingContext.hasOtherClasses()) {
      writer.append("    return Collections.emptyList();").eol();
    } else {
      writer.append("    List<Class<?>> others = new ArrayList<>();").eol();
      for (String otherType : processingContext.getOtherClasses()) {
        writer.append("    others.add(%s.class);", otherType).eol();
      }
      writer.append("    return others;").eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeMethodEntityClasses(Set<String> dbEntities, String dbName) {

    String modifier = "public";
    String method = "entityClasses";

    if (dbName == null) {
      writer.append("  @Override").eol();
    } else {
      method = dbName + "_entities";
      modifier = "private";
    }
    writer.append("  %s List<Class<?>> %s() {", modifier, method).eol();
    writer.append("    List<Class<?>> entities = new ArrayList<>();").eol();
    for (String dbEntity : dbEntities) {
      writer.append("    entities.add(%s.class);", dbEntity).eol();
    }
    if (processingContext.hasOtherClasses()) {
      writer.append("    entities.addAll(otherClasses());").eol();
    }
    writer.append("    return entities;").eol();
    writer.append("  }").eol().eol();
  }

  private void writeMethodEntityClassesFor(Set<String> otherDbNames) {

    writer.append("  @Override").eol();
    writer.append("  public List<Class<?>> entityClassesFor(String dbName) {").eol().eol();
    for (String dbName : otherDbNames) {
      writer.append("    if (\"%s\".equals(dbName)) return %s_entities();", dbName, dbName).eol();
    }
    writer.append("    return Collections.emptyList();").eol();
    writer.append("  }").eol().eol();
  }

  private void writeEndClass() {
    writer.append("}").eol();
  }

  private Writer createFileWriter() throws IOException {
    JavaFileObject jfo = processingContext.createWriter(factoryFullName);
    return jfo.openWriter();
  }
}
