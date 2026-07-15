package io.ebean.querybean.generator;

import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Writes the per-module {@code DtoMapperRegister} implementation aggregating all generated DTO
 * mappers, and registers it via {@code META-INF/services} - mirroring
 * {@link SimpleModuleInfoWriter}'s {@code EbeanEntityRegister} mechanism. Dispatch is via
 * literal {@code Class ==} comparisons only (no reflection/{@code Class.forName}), so this is
 * the compile-time-safe backing for the {@code query.mapTo(SomeDto.class)} runtime API.
 * <p>
 * Each generated mapper is held as a single shared instance field on the register (mappers are
 * stateless/thread-safe - only direct getter/constructor calls, no per-call state) rather than
 * being constructed with {@code new} on every {@code mapperFor()} call - this avoids repeatedly
 * rebuilding the nested mapper graph and {@code FetchGroup} on every {@code mapTo(...)} query.
 * Fields are declared in dependency order (a mapper's nested mappers are declared, and so
 * constructed, before it) so each field initializer can directly reference the already-built
 * nested mapper fields it depends on.
 */
class DtoMapperRegisterWriter {

  private static final String SHORT_NAME = "EbeanDtoMapperRegister";

  private final ProcessingContext ctx;
  private final List<DtoBeanMeta> metas;
  private final List<DtoBeanMeta> sortedMetas;
  private final String factoryPackage;
  private final String factoryFullName;
  private Append writer;

  DtoMapperRegisterWriter(ProcessingContext ctx, List<DtoBeanMeta> metas) {
    this.ctx = ctx;
    this.metas = metas;
    this.sortedMetas = sortByDependency(metas);
    this.factoryPackage = shortestMapperPackage(metas);
    this.factoryFullName = factoryPackage + "." + SHORT_NAME;
  }

  /**
   * Order {@code metas} so that every mapper appears after all the (nested) mappers its own
   * field initializer depends on - a dependency-first (topological) ordering over the
   * {@code NESTED_ONE}/{@code NESTED_MANY} edges. Cross-reference cycles were already detected
   * and excluded upstream in {@code DtoMappingReader}, so a simple DFS post-order is sufficient
   * (the {@code visited} guard is defensive only).
   */
  private static List<DtoBeanMeta> sortByDependency(List<DtoBeanMeta> metas) {
    List<DtoBeanMeta> sorted = new ArrayList<>(metas.size());
    Set<DtoBeanMeta> visited = new HashSet<>();
    for (DtoBeanMeta meta : metas) {
      visitForSort(meta, visited, sorted);
    }
    return sorted;
  }

  private static void visitForSort(DtoBeanMeta meta, Set<DtoBeanMeta> visited, List<DtoBeanMeta> sorted) {
    if (!visited.add(meta)) {
      return;
    }
    for (DtoPropertyMeta property : meta.properties()) {
      DtoBeanMeta nested = property.nested();
      if (nested != null) {
        visitForSort(nested, visited, sorted);
      }
    }
    sorted.add(meta);
  }

  /**
   * Shortest of the (per-pair) mapper packages, used as the single package the aggregating
   * {@code EbeanDtoMapperRegister} is written to - mirrors {@code SimpleModuleInfoWriter}'s
   * entity-package heuristic, but computed from the dto mapper packages themselves rather than
   * {@code ProcessingContext.getFactoryPackage()} (which only tracks {@code @Entity} packages
   * and is never populated during a test-source-only compilation round, which would otherwise
   * make this fall back to its "unknown" default).
   */
  private static String shortestMapperPackage(List<DtoBeanMeta> metas) {
    String shortest = null;
    for (DtoBeanMeta meta : metas) {
      String pkg = meta.mapperPackage();
      if (shortest == null || pkg.length() < shortest.length()) {
        shortest = pkg;
      }
    }
    return shortest;
  }

  void write() throws IOException {
    if (hasSimpleNameCollision()) {
      return;
    }
    var javaFileObject = ctx.createWriter(factoryFullName);
    writer = new Append(javaFileObject.openWriter());
    writePackage();
    writeClassStart();
    writeFields();
    writeMapperForMethod();
    writeMapperOfTypeMethod();
    writeClassEnd();
    writer.close();
    writeServicesFile();
  }

  /**
   * Fail fast, with a clear diagnostic, if two different types referenced by the aggregated
   * register (across every {@code @DtoMapping} pair in this module) share the same simple class
   * name - dispatch and imports are both simple-name based (see {@link #writePackage()}), so an
   * undetected collision would otherwise surface as a confusing duplicate-import compile error
   * in the generated source rather than a clear message pointing at the actual cause.
   */
  private boolean hasSimpleNameCollision() {
    Set<String> allReferenced = new LinkedHashSet<>();
    for (DtoBeanMeta meta : metas) {
      allReferenced.add(meta.sourceFullName());
      allReferenced.add(meta.targetFullName());
      allReferenced.add(meta.mapperFullName());
      for (DtoConverterMeta converter : meta.converterDeps()) {
        allReferenced.add(converter.typeFullName());
      }
    }
    String[] collision = Split.findSimpleNameCollision(allReferenced);
    if (collision != null) {
      ctx.logError(null, "@DtoMapping simple name collision generating %s - %s and %s share the same"
        + " simple class name; this is not currently supported within one module, use distinct class names",
        SHORT_NAME, collision[0], collision[1]);
      return true;
    }
    return false;
  }

  private void writePackage() {
    writer.append("package %s;", factoryPackage).eol().eol();
    Set<String> imports = new LinkedHashSet<>();
    imports.add("io.ebean.DtoMapper");
    imports.add("io.ebean.config.DtoMapperRegister");
    for (DtoBeanMeta meta : metas) {
      addImportIfNeeded(imports, meta.sourceFullName());
      addImportIfNeeded(imports, meta.targetFullName());
      addImportIfNeeded(imports, meta.mapperFullName());
      for (DtoConverterMeta converter : meta.converterDeps()) {
        addImportIfNeeded(imports, converter.typeFullName());
      }
      if (!meta.converterDeps().isEmpty()) {
        imports.add("io.ebean.DtoConverterManager");
      }
    }
    for (String imp : imports) {
      writer.append("import %s;", imp).eol();
    }
    writer.eol();
  }

  private void addImportIfNeeded(Set<String> imports, String fullName) {
    if (!Split.split(fullName)[0].equals(factoryPackage)) {
      imports.add(fullName);
    }
  }

  private void writeClassStart() {
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("public class %s implements DtoMapperRegister {", SHORT_NAME).eol().eol();
  }

  /**
   * Declare one field per generated mapper, in dependency order, each initialized exactly once -
   * a single shared instance reused by every {@code mapperFor()} call rather than a new instance
   * per call. A mapper with nested (ToOne/ToMany) properties and/or instance-dispatch
   * {@code @DtoConvert} converters is constructed via its all-args constructor, passing the
   * already-declared nested mapper fields and {@code DtoConverterManager.get(...)}-resolved
   * converter instances it depends on.
   */
  private void writeFields() {
    for (DtoBeanMeta meta : sortedMetas) {
      List<DtoBeanMeta> deps = nestedMetas(meta);
      List<DtoConverterMeta> converters = meta.converterDeps();
      String shortName = meta.mapperShortName();
      String fieldName = mapperFieldName(meta);
      if (deps.isEmpty() && converters.isEmpty()) {
        writer.append("  private final %s %s = new %s();", shortName, fieldName, shortName).eol();
      } else {
        StringBuilder args = new StringBuilder();
        for (DtoBeanMeta dep : deps) {
          if (args.length() > 0) {
            args.append(", ");
          }
          args.append(mapperFieldName(dep));
        }
        for (DtoConverterMeta converter : converters) {
          if (args.length() > 0) {
            args.append(", ");
          }
          args.append("DtoConverterManager.get(").append(converter.typeShortName()).append(".class)");
        }
        writer.append("  private final %s %s = new %s(%s);", shortName, fieldName, shortName, args).eol();
      }
    }
    writer.eol();
  }

  /**
   * The other {@link DtoBeanMeta}s a mapper's all-args constructor depends on, in the same order
   * {@link DtoMapperWriter} declares its nested mapper constructor parameters (declaration order
   * of the target DTO's {@code NESTED_ONE}/{@code NESTED_MANY} properties).
   */
  private List<DtoBeanMeta> nestedMetas(DtoBeanMeta meta) {
    List<DtoBeanMeta> nested = new ArrayList<>();
    for (DtoPropertyMeta property : meta.properties()) {
      if (property.nested() != null) {
        nested.add(property.nested());
      }
    }
    return nested;
  }

  private String mapperFieldName(DtoBeanMeta meta) {
    String name = meta.mapperShortName();
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }

  private void writeMapperForMethod() {
    writer.append("  @Override").eol();
    writer.append("  @SuppressWarnings(\"unchecked\")").eol();
    writer.append("  public <SOURCE, TARGET> DtoMapper<SOURCE, TARGET> mapperFor(Class<SOURCE> sourceType, Class<TARGET> targetType) {").eol();
    for (DtoBeanMeta meta : metas) {
      String sourceShort = Split.shortName(meta.sourceFullName());
      String targetShort = Split.shortName(meta.targetFullName());
      writer.append("    if (sourceType == %s.class && targetType == %s.class) return (DtoMapper<SOURCE, TARGET>) %s;",
        sourceShort, targetShort, mapperFieldName(meta)).eol();
    }
    writer.append("    return null;").eol();
    writer.append("  }").eol();
  }

  /**
   * Look up a mapper instance by its own concrete generated type - e.g. {@code
   * DtoMapperManager.get(CustomerDtoMapper.class)} - as an alternative to {@link
   * #writeMapperForMethod()}'s (source, target) pair lookup, typically used to resolve a mapper
   * instance for dependency injection into application code.
   */
  private void writeMapperOfTypeMethod() {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  @SuppressWarnings(\"unchecked\")").eol();
    writer.append("  public <T> T mapperOfType(Class<T> mapperType) {").eol();
    for (DtoBeanMeta meta : metas) {
      writer.append("    if (mapperType == %s.class) return (T) %s;", meta.mapperShortName(), mapperFieldName(meta)).eol();
    }
    writer.append("    return null;").eol();
    writer.append("  }").eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeServicesFile() {
    try {
      FileObject jfo = ctx.createMetaInfWriter(Constants.METAINF_SERVICES_DTOMAPPERREGISTER);
      if (jfo != null) {
        Writer w = jfo.openWriter();
        w.write(factoryFullName);
        w.close();
      }
    } catch (IOException e) {
      ctx.logError(null, "Failed to write DtoMapperRegister services file " + e.getMessage());
    }
  }
}
