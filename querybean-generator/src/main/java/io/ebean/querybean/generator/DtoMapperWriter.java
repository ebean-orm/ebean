package io.ebean.querybean.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes a single generated {@code XxxMapper.java} implementing {@code io.ebean.DtoMapper<S,T>}
 * for one {@link DtoBeanMeta}, in the exact shape validated by the hand-written
 * dto-spike-toone-mapper / dto-spike-tomany-identity spikes: no reflection, nested mappers wired
 * via constructor injection (a no-arg constructor supplying default instances, plus an explicit
 * overload for substitution in tests). Identity de-duplication via the shared
 * {@code DtoMapContext}'s {@code computeIfAbsent} is only generated for types that are actually
 * nested elsewhere in the DTO graph (see {@link DtoBeanMeta#nestedElsewhere()}) - a pure
 * top-level type constructs its DTO directly, since the cache could never produce a hit for it.
 */
class DtoMapperWriter {

  private final ProcessingContext ctx;
  private final DtoBeanMeta meta;
  private Append writer;

  DtoMapperWriter(ProcessingContext ctx, DtoBeanMeta meta) {
    this.ctx = ctx;
    this.meta = meta;
  }

  void write() throws IOException {
    if (hasSimpleNameCollision()) {
      return;
    }
    var javaFileObject = ctx.createWriter(meta.mapperFullName(), meta.target());
    writer = new Append(javaFileObject.openWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeConstructors();
    writeFetchGroupMethod();
    writeMapMethod();
    if (!meta.variants().isEmpty()) {
      writeBuildMethod();
      writeVariantAccessors();
    }
    writeClassEnd();
    writer.close();
  }

  /**
   * Fail fast, with a clear diagnostic, if two different types referenced by this single
   * generated mapper (source, target, any nested source/target/mapper, any converter type) share
   * the same simple class name - {@link #writeImports()} relies on simple names being unambiguous
   * within the file, so an undetected collision would otherwise surface as a confusing
   * duplicate-import compile error rather than a clear message pointing at the actual cause.
   */
  private boolean hasSimpleNameCollision() {
    Set<String> allReferenced = new LinkedHashSet<>();
    allReferenced.add(meta.sourceFullName());
    allReferenced.add(meta.targetFullName());
    for (DtoPropertyMeta property : meta.properties()) {
      if (property.nested() != null) {
        allReferenced.add(property.nested().sourceFullName());
        allReferenced.add(property.nested().targetFullName());
        allReferenced.add(property.nested().mapperFullName());
      }
      if (property.converter() != null) {
        allReferenced.add(property.converter().typeFullName());
      }
    }
    String[] collision = Split.findSimpleNameCollision(allReferenced);
    if (collision != null) {
      ctx.logError(meta.target(), "@DtoMapping simple name collision generating %s - %s and %s share the"
        + " same simple class name; this is not currently supported within one generated mapper, use"
        + " distinct class names", meta.mapperShortName(), collision[0], collision[1]);
      return true;
    }
    return false;
  }

  private void writePackage() {
    writer.append("package %s;", meta.mapperPackage()).eol().eol();
  }

  private void writeImports() {
    Set<String> imports = new LinkedHashSet<>();
    imports.add("io.ebean.DtoMapper");
    imports.add("io.ebean.DtoMapContext");
    imports.add("io.ebean.FetchGroup");
    addImportIfNeeded(imports, meta.sourceFullName());
    addImportIfNeeded(imports, meta.targetFullName());
    for (DtoPropertyMeta property : meta.properties()) {
      if (property.nested() != null) {
        addImportIfNeeded(imports, property.nested().sourceFullName());
        addImportIfNeeded(imports, property.nested().targetFullName());
        addImportIfNeeded(imports, property.nested().mapperFullName());
      }
      if (property.converter() != null) {
        addImportIfNeeded(imports, property.converter().typeFullName());
      }
    }
    if (!meta.converterDeps().isEmpty()) {
      imports.add("io.ebean.DtoConverterManager");
    }
    if (meta.properties().stream().anyMatch(DtoPropertyMeta::usesMapperSupport)) {
      imports.add("io.ebean.DtoMapperSupport");
    }
    if (variableProperties().stream().anyMatch(p -> p.kind() == DtoPropertyMeta.Kind.NESTED_MANY)) {
      imports.add("java.util.List");
    }
    for (String imp : imports) {
      writer.append("import %s;", imp).eol();
    }
    writer.eol();
  }

  private void addImportIfNeeded(Set<String> imports, String fullName) {
    String pkg = Split.split(fullName)[0];
    if (pkg != null && !pkg.equals(meta.mapperPackage())) {
      imports.add(fullName);
    }
  }

  private void writeClassStart() {
    String sourceShort = Split.shortName(meta.sourceFullName());
    String targetShort = Split.shortName(meta.targetFullName());
    writer.append(Constants.AT_GENERATED).eol();
    writer.append("public final class %s implements DtoMapper<%s, %s> {", meta.mapperShortName(),
      sourceShort, targetShort).eol().eol();
    for (DtoPropertyMeta property : nestedProperties()) {
      writer.append("  private final DtoMapper<%s, %s> %s;",
        Split.shortName(property.nested().sourceFullName()),
        Split.shortName(property.nested().targetFullName()),
        mapperFieldName(property)).eol();
    }
    for (DtoConverterMeta converter : meta.converterDeps()) {
      writer.append("  private final %s %s;", converter.typeShortName(), converter.fieldName()).eol();
    }
    writer.append("  private final FetchGroup<%s> fetchGroup;", sourceShort).eol();
    for (DtoMapperVariant variant : meta.variants()) {
      writer.append("  private final FetchGroup<%s> %s;", sourceShort, variant.fetchGroupFieldName()).eol();
    }
    // one shared, stateless instance per variant - same "singleton, not `new` per call" approach
    // DtoMapperRegisterWriter already uses for the top-level generated mappers themselves
    for (DtoMapperVariant variant : meta.variants()) {
      writer.append("  private final DtoMapper<%s, %s> %s;", sourceShort, targetShort, variant.fieldName()).eol();
    }
    writer.eol();
  }

  private void writeConstructors() {
    var nested = nestedProperties();
    var converters = meta.converterDeps();
    if (nested.isEmpty() && converters.isEmpty()) {
      writer.append("  public %s() {", meta.mapperShortName()).eol();
      writeAllFetchGroupAssignments("    ");
      writeAllVariantViewAssignments("    ");
      writer.append("  }").eol().eol();
      return;
    }
    writer.append("  public %s() {", meta.mapperShortName()).eol();
    StringBuilder args = new StringBuilder();
    for (DtoPropertyMeta property : nested) {
      if (args.length() > 0) {
        args.append(", ");
      }
      args.append("new ").append(property.nested().mapperShortName()).append("()");
    }
    for (DtoConverterMeta converter : converters) {
      if (args.length() > 0) {
        args.append(", ");
      }
      args.append("DtoConverterManager.get(").append(converter.typeShortName()).append(".class)");
    }
    writer.append("    this(%s);", args).eol();
    writer.append("  }").eol().eol();

    writer.append("  public %s(", meta.mapperShortName());
    StringBuilder params = new StringBuilder();
    for (DtoPropertyMeta property : nested) {
      if (params.length() > 0) {
        params.append(", ");
      }
      params.append("DtoMapper<").append(Split.shortName(property.nested().sourceFullName()))
        .append(", ").append(Split.shortName(property.nested().targetFullName())).append("> ")
        .append(mapperFieldName(property));
    }
    for (DtoConverterMeta converter : converters) {
      if (params.length() > 0) {
        params.append(", ");
      }
      params.append(converter.typeShortName()).append(" ").append(converter.fieldName());
    }
    writer.append("%s) {", params).eol();
    for (DtoPropertyMeta property : nested) {
      writer.append("    this.%s = %s;", mapperFieldName(property), mapperFieldName(property)).eol();
    }
    for (DtoConverterMeta converter : converters) {
      writer.append("    this.%s = %s;", converter.fieldName(), converter.fieldName()).eol();
    }
    writeAllFetchGroupAssignments("    ");
    writeAllVariantViewAssignments("    ");
    writer.append("  }").eol().eol();
  }

  /**
   * Build the list of {@code .select(...)}/{@code .fetch(...)} chain calls (without the leading
   * dot) needed to populate the target DTO - derived from the DTO's declared shape rather than
   * maintained by hand. {@code REF} properties (the {@code @DtoRef} id-only escape hatch) add
   * their association's name to the root {@code select(...)} - this reads the FK column directly
   * off the base table (no join, unlike {@code .fetch(assoc, "id")}) and, importantly, actually
   * populates it rather than relying on it being "already there" (which only holds when some
   * other property on this same DTO independently fetches that association). Skipped only when
   * the same association is already fully fetched via a {@code NESTED_ONE}/{@code NESTED_MANY}
   * property, to avoid a redundant/duplicate select of a path that's already covered by a fetch.
   */
  private List<String> fetchGroupChainCalls(Set<String> excludedFieldNames) {
    List<DtoPropertyMeta> activeProperties = new ArrayList<>();
    for (DtoPropertyMeta property : meta.properties()) {
      if (!excludedFieldNames.contains(property.dtoFieldName())) {
        activeProperties.add(property);
      }
    }
    Set<String> nestedAssocPaths = new LinkedHashSet<>();
    for (DtoPropertyMeta property : activeProperties) {
      if ((property.kind() == DtoPropertyMeta.Kind.NESTED_ONE || property.kind() == DtoPropertyMeta.Kind.NESTED_MANY)
        && !property.hasComputedSegment()) {
        nestedAssocPaths.add(property.sourcePropertyPath().get(0));
      }
    }
    Set<String> rootSelect = new LinkedHashSet<>();
    Map<String, List<String>> pathSelect = new LinkedHashMap<>();
    Set<String> extraFetchPaths = new LinkedHashSet<>();
    List<String> fetchCalls = new ArrayList<>();
    for (DtoPropertyMeta property : activeProperties) {
      switch (property.kind()) {
        case NESTED_ONE:
        case NESTED_MANY:
          if (property.hasComputedSegment()) {
            // a single-hop @DtoPath rename traversing a computed/derived getter (no backing
            // field) that happens to target a nested DTO type - just as unfetchable via
            // fetch(path, mapper.fetchGroup()) as the analogous SCALAR case, since "path" here
            // isn't a real Ebean fetch path either. The nested mapper is still invoked directly
            // against whatever the getter returns (see DtoMapperWriter#propertyValueExpression) -
            // it's purely the FetchGroup derivation that must fall back to @DtoPath#requires().
            extraFetchPaths.addAll(property.requiredFetchPaths());
            break;
          }
          fetchCalls.add(String.format("fetch(\"%s\", %s.fetchGroup())",
            property.sourcePropertyPath().get(0), mapperFieldName(property)));
          break;
        case SCALAR:
          if (property.hasComputedSegment()) {
            // the path traverses a computed/derived getter (no backing field) - its own segments
            // past that point aren't real Ebean fetch paths, so don't add them to pathSelect/
            // rootSelect at all; @DtoPath#requires() (plus the real prefix, if any) already names
            // exactly what needs fetching instead - see DtoPropertyMeta#requiredFetchPaths().
            extraFetchPaths.addAll(property.requiredFetchPaths());
            break;
          }
          List<String> path = property.sourcePropertyPath();
          if (path.size() == 1) {
            rootSelect.add(path.get(0));
          } else {
            String fetchPath = String.join(".", path.subList(0, path.size() - 1));
            if (nestedAssocPaths.contains(fetchPath)) {
              if (excludedFieldNames.isEmpty()) {
                reportFetchPathCollision(property, fetchPath);
              }
              break;
            }
            pathSelect.computeIfAbsent(fetchPath, p -> new ArrayList<>())
              .add(path.get(path.size() - 1));
          }
          break;
        case REF:
        default:
          if (property.hasComputedSegment()) {
            // the association has no backing field (a computed/derived getter) - "assoc" isn't a
            // real Ebean property name, so it can't be handed to FetchGroup.select(...) directly;
            // @DtoRef#requires() already names exactly what needs fetching instead - see
            // DtoPropertyMeta#requiredFetchPaths(). The value mapping itself (source.getAssoc().
            // getId()) still works via plain Java method invocation regardless.
            extraFetchPaths.addAll(property.requiredFetchPaths());
            break;
          }
          String assoc = property.sourcePropertyPath().get(0);
          if (!nestedAssocPaths.contains(assoc)) {
            rootSelect.add(assoc);
          }
          break;
      }
    }
    for (var entry : pathSelect.entrySet()) {
      if (extraFetchPaths.contains(entry.getKey())) {
        // an unrelated computed-segment property also needs a bare, full fetch(path) at this
        // exact same path (emitted below) - FetchGroup's builder REPLACES (not merges) same-path
        // fetch calls (OrmQueryDetail.fetch(...) is a plain Map.put keyed by path), so emitting
        // both a narrowed fetch(path, "props") here and a bare fetch(path) below would leave
        // only whichever call happens to be added last in effect, silently discarding the other's
        // requirement depending on emission order. Skip the narrow entry - a full fetch(path) is
        // always a safe superset of any narrower property selection, so let the bare fetch below
        // win deterministically instead of depending on iteration order.
        continue;
      }
      fetchCalls.add(String.format("fetch(\"%s\", \"%s\")", entry.getKey(), String.join(",", entry.getValue())));
    }
    for (String extraPath : extraFetchPaths) {
      // already covered by another property's NESTED_ONE/MANY fetch of the exact same path (a
      // full nested mapper.fetchGroup()) - that's richer than a bare fetch(path) (which would
      // replace it and lose the nested mapper's own fetch requirements), so it must win instead.
      if (!nestedAssocPaths.contains(extraPath)) {
        fetchCalls.add(String.format("fetch(\"%s\")", extraPath));
      }
    }
    List<String> calls = new ArrayList<>();
    if (!rootSelect.isEmpty()) {
      calls.add(String.format("select(\"%s\")", String.join(",", rootSelect)));
    }
    calls.addAll(fetchCalls);
    return calls;
  }

  /**
   * Fail fast at codegen time rather than silently generating broken code: {@code OrmQueryDetail}
   * keys its fetch paths by exact path string and {@code fetch(path, ...)} replaces (not merges)
   * any existing entry for that same key - so a {@code @DtoPath} property whose fetch path is
   * identical to an existing {@code NESTED_ONE}/{@code NESTED_MANY} property's own fetch path would
   * silently overwrite (or be overwritten by) that nested fetch, discarding whichever call happens
   * to be emitted first/last. Detected here instead so the failure is a clear compile-time error.
   */
  private void reportFetchPathCollision(DtoPropertyMeta property, String fetchPath) {
    ctx.logError(meta.target(),
      "@DtoPath property '%s' on %s resolves to fetch path '%s', which collides with the nested"
        + " mapping already using that same fetch path - Ebean's fetch spec can only carry one set"
        + " of properties per path, so one silently discards the other. Move '%s' onto the nested"
        + " DTO type instead (its own mapper's fetchGroup already fetches path '%s'), or choose a"
        + " @DtoPath that reaches into a different, non-colliding path.",
      property.dtoFieldName(), meta.targetFullName(), fetchPath, property.dtoFieldName(), fetchPath);
  }

  /**
   * Write the {@code this.fetchGroup = FetchGroup.of(Source.class)...build();} assignment (and
   * one per named {@link DtoMapperVariant}, each skipping its own excluded properties from the
   * fetch spec), with each chained {@code .select()}/{@code .fetch()}/{@code .build()} call on
   * its own line for readability.
   */
  private void writeAllFetchGroupAssignments(String indent) {
    writeFetchGroupAssignment(indent, "fetchGroup", Set.of());
    for (DtoMapperVariant variant : meta.variants()) {
      writeFetchGroupAssignment(indent, variant.fetchGroupFieldName(), variant.excludedProperties());
    }
  }

  private void writeFetchGroupAssignment(String indent, String fieldName, Set<String> excludedFieldNames) {
    writer.append("%sthis.%s = FetchGroup.of(%s.class)", indent, fieldName, Split.shortName(meta.sourceFullName()));
    for (String call : fetchGroupChainCalls(excludedFieldNames)) {
      writer.eol().append("%s  .%s", indent, call);
    }
    writer.eol().append("%s  .build();", indent).eol();
  }

  /**
   * Assign each named variant's shared, stateless inner-class instance (its {@code DtoMapper}
   * view) once, in the constructor - a single instance is reused across every call to the
   * variant's accessor method (e.g. {@code noFleets()}), rather than allocating a fresh one per
   * call, matching {@code DtoMapperRegisterWriter}'s "singleton, not {@code new} per call"
   * approach for the top-level generated mappers themselves.
   */
  private void writeAllVariantViewAssignments(String indent) {
    for (DtoMapperVariant variant : meta.variants()) {
      writer.append("%sthis.%s = new %s();", indent, variant.fieldName(), variant.innerClassName()).eol();
    }
  }

  private void writeFetchGroupMethod() {
    writer.append("  @Override").eol();
    writer.append("  public FetchGroup<%s> fetchGroup() {", Split.shortName(meta.sourceFullName())).eol();
    writer.append("    return fetchGroup;").eol();
    writer.append("  }").eol().eol();
  }

  private void writeMapMethod() {
    String sourceShort = Split.shortName(meta.sourceFullName());
    String targetShort = Split.shortName(meta.targetFullName());
    Set<DtoPropertyMeta> variableProps = variableProperties();
    writer.append("  @Override").eol();
    writer.append("  public %s map(%s source, DtoMapContext context) {", targetShort, sourceShort).eol();
    writer.append("    if (source == null) {").eol();
    writer.append("      return null;").eol();
    writer.append("    }").eol();
    if (!variableProps.isEmpty()) {
      // named variants exist - route construction through the shared build(...) method, this
      // (base) mapping includes every variable property (each still evaluated inline, at its own
      // declared position, inside build() - see buildCallArgs())
      if (meta.nestedElsewhere()) {
        writer.append("    // dedup using DtoMapContext, same %s instance can be reached via more than one path in the graph", sourceShort).eol();
        writer.append("    return context.computeIfAbsent(%s.class, source, s -> build(s, context%s));",
          targetShort, buildCallArgs(variableProps)).eol();
      } else {
        writer.append("    // DtoMapContext for nested mappers only").eol();
        writer.append("    return build(source, context%s);", buildCallArgs(variableProps)).eol();
      }
    } else if (meta.nestedElsewhere()) {
      writer.append("    // dedup using DtoMapContext, same %s instance can be reached via more than one path in the graph", sourceShort).eol();
      writeConstructionExpression(
        String.format("    return context.computeIfAbsent(%s.class, source, s -> ", targetShort),
        "s", variableProps, ")");
    } else if (nestedProperties().isEmpty()) {
      writer.append("    // skip DtoMapContext, only ever a top-level mapping").eol();
      writeConstructionExpression("    return ", "source", variableProps, "");
    } else {
      writer.append("    // DtoMapContext for nested mappers only").eol();
      writeConstructionExpression("    return ", "source", variableProps, "");
    }
    writer.append("  }").eol();
  }

  /**
   * Emit the target construction expression - either a positional constructor call or, when
   * {@link DtoBeanMeta#builderMeta()} is present (see {@code @DtoMapping(builder = ...)}), a
   * {@code Target.builder()....build()} fluent chain. {@code linePrefix} is written before the
   * expression starts (e.g. {@code "    return "} or a {@code computeIfAbsent(...)} lambda
   * opener); {@code wrapperClosing} is any additional closing needed for an enclosing call this
   * expression is nested inside (e.g. {@code ")"} to close an enclosing
   * {@code computeIfAbsent(...)} call, or {@code ""} for a plain {@code return}) - the trailing
   * {@code ";"} is always added here. Every property - including ones in {@code variableProps} -
   * is evaluated inline, in true {@link DtoBeanMeta#properties()} declared order; a variable
   * property's expression is simply guarded by its {@code includeXxx} boolean parameter (see
   * {@link #writeBuildMethod()}), so no property's evaluation is ever hoisted out of its declared
   * position (only non-empty when named variants exist - see {@link #variableProperties()}).
   */
  private void writeConstructionExpression(String linePrefix, String rootVariable, Set<DtoPropertyMeta> variableProps, String wrapperClosing) {
    String targetShort = Split.shortName(meta.targetFullName());
    if (meta.builderMeta() == null) {
      writer.append("%snew %s(", linePrefix, targetShort).eol();
      writeConstructionArgs(rootVariable, variableProps, ")" + wrapperClosing + ";");
    } else {
      writer.append("%s%s.builder()", linePrefix, targetShort).eol();
      writeBuilderChain(rootVariable, variableProps);
      writer.append("      .build()%s;", wrapperClosing).eol();
    }
  }

  private void writeConstructionArgs(String rootVariable, Set<DtoPropertyMeta> variableProps, String closing) {
    var properties = meta.properties();
    for (int i = 0; i < properties.size(); i++) {
      DtoPropertyMeta property = properties.get(i);
      writer.append("      %s%s", constructionValueExpression(property, rootVariable, variableProps),
        i < properties.size() - 1 ? "," : closing).eol();
    }
    if (properties.isEmpty()) {
      writer.append("      %s", closing).eol();
    }
  }

  private void writeBuilderChain(String rootVariable, Set<DtoPropertyMeta> variableProps) {
    for (DtoPropertyMeta property : meta.properties()) {
      writer.append("      .%s(%s)", property.dtoFieldName(), constructionValueExpression(property, rootVariable, variableProps)).eol();
    }
  }

  /**
   * A variable property (excluded by at least one named variant) is guarded by its own
   * {@code includeXxx} boolean parameter (see {@link #writeBuildMethod()}) - a ternary choosing
   * between its real computed expression and its empty default - so it stays evaluated inline, at
   * its true declared position, exactly like a non-variable property. This preserves the DTO's
   * declared property order as the actual evaluation order, regardless of which properties happen
   * to be excluded by a variant.
   */
  private String constructionValueExpression(DtoPropertyMeta property, String rootVariable, Set<DtoPropertyMeta> variableProps) {
    String expression = propertyValueExpression(property, rootVariable);
    return variableProps.contains(property)
      ? includeFlagName(property) + " ? " + expression + " : " + defaultValueFor(property)
      : expression;
  }

  /**
   * The set of properties (in {@link DtoBeanMeta#properties()} order) excluded by at least one
   * {@link DtoMapperVariant} - each becomes an extra {@code boolean includeXxx} parameter on the
   * shared {@link #writeBuildMethod()} (rather than a precomputed value), so build() itself
   * decides - at the property's own declared position - whether to compute it or substitute its
   * empty default. Empty when {@link DtoBeanMeta#variants()} is empty.
   */
  private Set<DtoPropertyMeta> variableProperties() {
    Set<DtoPropertyMeta> result = new LinkedHashSet<>();
    for (DtoPropertyMeta property : meta.properties()) {
      for (DtoMapperVariant variant : meta.variants()) {
        if (variant.excludes(property)) {
          result.add(property);
          break;
        }
      }
    }
    return result;
  }

  /** {@code true} literal (comma-prefixed) per variable property - the base mapping includes every property. */
  private String buildCallArgs(Set<DtoPropertyMeta> variableProps) {
    StringBuilder sb = new StringBuilder();
    for (DtoPropertyMeta property : variableProps) {
      sb.append(", true");
    }
    return sb.toString();
  }

  /**
   * The private construction helper shared by the base {@code map()} and every named variant -
   * written once, so the common field population logic isn't duplicated per variant. Each
   * variable property (see {@link #variableProperties()}) is accepted as a {@code boolean
   * includeXxx} parameter rather than a precomputed value - build() evaluates every property
   * inline, at its own declared position, choosing the real expression or the empty default based
   * on the flag - so introducing variants never changes evaluation order relative to the DTO's
   * declared property order.
   */
  private void writeBuildMethod() {
    Set<DtoPropertyMeta> variableProps = variableProperties();
    String sourceShort = Split.shortName(meta.sourceFullName());
    String targetShort = Split.shortName(meta.targetFullName());
    writer.append("  private %s build(%s source, DtoMapContext context%s) {",
      targetShort, sourceShort, buildMethodParams(variableProps)).eol();
    writeConstructionExpression("    return ", "source", variableProps, "");
    writer.append("  }").eol().eol();
  }

  private String buildMethodParams(Set<DtoPropertyMeta> variableProps) {
    StringBuilder sb = new StringBuilder();
    for (DtoPropertyMeta property : variableProps) {
      sb.append(", boolean ").append(includeFlagName(property));
    }
    return sb.toString();
  }

  /** Parameter/flag name for a variable property, e.g. {@code contacts} -> {@code includeContacts}. */
  private String includeFlagName(DtoPropertyMeta property) {
    String name = property.dtoFieldName();
    return "include" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * The empty/default value a variant supplies for a property it excludes - {@code null} for a
   * {@code NESTED_ONE}, {@code List.of()} for a {@code NESTED_MANY} (always valid since
   * {@code NESTED_MANY} only ever derives from a {@code java.util.List<X>} target property).
   */
  private String defaultValueFor(DtoPropertyMeta property) {
    return property.kind() == DtoPropertyMeta.Kind.NESTED_MANY ? "List.of()" : "null";
  }

  /**
   * Write each {@link DtoMapperVariant}'s public accessor method (returning its own
   * {@code DtoMapper<SOURCE, TARGET>} view, e.g. {@code noFleets()}) followed by its small inner
   * class delegating back to the shared {@link #writeBuildMethod()}. Deliberately skips the
   * {@code DtoMapContext} identity-dedup wrapper used by the base mapping when
   * {@link DtoBeanMeta#nestedElsewhere()} - named variants are only ever used as an independent,
   * top-level {@code query.mapTo(...)} result, never nested inside another DTO graph.
   */
  private void writeVariantAccessors() {
    Set<DtoPropertyMeta> variableProps = variableProperties();
    String sourceShort = Split.shortName(meta.sourceFullName());
    String targetShort = Split.shortName(meta.targetFullName());
    for (DtoMapperVariant variant : meta.variants()) {
      writer.append("  /** Named variant excluding {%s} - see @DtoMapping(name = \"%s\", exclude = ...). */",
        String.join(", ", variant.excludedProperties()), variant.name()).eol();
      writer.append("  public DtoMapper<%s, %s> %s() {", sourceShort, targetShort, variant.name()).eol();
      writer.append("    return %s;", variant.fieldName()).eol();
      writer.append("  }").eol().eol();
    }
    for (DtoMapperVariant variant : meta.variants()) {
      writeVariantInnerClass(variant, variableProps, sourceShort, targetShort);
    }
  }

  private void writeVariantInnerClass(DtoMapperVariant variant, Set<DtoPropertyMeta> variableProps, String sourceShort, String targetShort) {
    writer.append("  private final class %s implements DtoMapper<%s, %s> {", variant.innerClassName(), sourceShort, targetShort).eol().eol();
    writer.append("    @Override").eol();
    writer.append("    public FetchGroup<%s> fetchGroup() {", sourceShort).eol();
    writer.append("      return %s;", variant.fetchGroupFieldName()).eol();
    writer.append("    }").eol().eol();
    writer.append("    @Override").eol();
    writer.append("    public %s map(%s source, DtoMapContext context) {", targetShort, sourceShort).eol();
    writer.append("      if (source == null) {").eol();
    writer.append("        return null;").eol();
    writer.append("      }").eol();
    writer.append("      return build(source, context%s);", variantBuildArgs(variant, variableProps)).eol();
    writer.append("    }").eol();
    writer.append("  }").eol().eol();
  }

  /** {@code true}/{@code false} literal (comma-prefixed) per variable property - {@code false} for ones this variant excludes. */
  private String variantBuildArgs(DtoMapperVariant variant, Set<DtoPropertyMeta> variableProps) {
    StringBuilder sb = new StringBuilder();
    for (DtoPropertyMeta property : variableProps) {
      sb.append(", ").append(!variant.excludes(property));
    }
    return sb.toString();
  }
  private String propertyValueExpression(DtoPropertyMeta property, String rootVariable) {
    switch (property.kind()) {
      case NESTED_ONE:
        return mapperFieldName(property) + ".map(" + property.sourceValueExpression(rootVariable) + ", context)";
      case NESTED_MANY:
        return mapperFieldName(property) + ".mapList(" + property.sourceValueExpression(rootVariable) + ", context)";
      case SCALAR:
      case REF:
      default:
        return applyConverter(property, property.sourceValueExpression(rootVariable));
    }
  }

  /**
   * Wrap {@code rawExpression} in the property's {@code @DtoConvert} conversion call, if any -
   * a direct static call ({@code ConverterType.method(rawExpression)}) or a call against the
   * resolved instance field ({@code converterField.method(rawExpression)}) depending on
   * {@link DtoConverterMeta#isStatic()}.
   */
  private String applyConverter(DtoPropertyMeta property, String rawExpression) {
    DtoConverterMeta converter = property.converter();
    if (converter == null) {
      return rawExpression;
    }
    String receiver = converter.isStatic() ? converter.typeShortName() : converter.fieldName();
    return receiver + "." + converter.methodName() + "(" + rawExpression + ")";
  }

  private List<DtoPropertyMeta> nestedProperties() {
    List<DtoPropertyMeta> nested = new ArrayList<>();
    for (DtoPropertyMeta property : meta.properties()) {
      if (property.nested() != null) {
        nested.add(property);
      }
    }
    return nested;
  }

  private String mapperFieldName(DtoPropertyMeta property) {
    return property.dtoFieldName() + "Mapper";
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }
}
