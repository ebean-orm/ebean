package io.ebean.querybean.generator;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads all {@code @DtoMapping(source, target)} declarations present in the current round,
 * builds a {@link DtoBeanMeta} (with matched properties) for each pair, and excludes any that
 * take part in a cycle in the nested-DTO type graph (logging a clear error for each).
 */
class DtoMappingReader {

  /** Auto-detection threshold for {@code @DtoMapping(builder = AUTO)} - see {@link #resolveBuilder}. */
  private static final int BUILDER_AUTO_THRESHOLD = 5;

  private final ProcessingContext ctx;
  private final Map<String, DtoBeanMeta> byTargetName = new LinkedHashMap<>();

  /**
   * {@code @DtoMixin}-annotated companion types, keyed by the (qualified name of the) real DTO
   * type they overlay annotations onto - see {@link #collectMixins(RoundEnvironment)}.
   */
  private final Map<String, TypeElement> mixinsByTarget = new LinkedHashMap<>();

  /**
   * Raw {@code @DtoMapping(name = "...", exclude = "...")} variant registrations, keyed by
   * target FQN - resolved into {@link DtoMapperVariant}s (attached to the matching base
   * {@link DtoBeanMeta}) once properties are known, in {@link #resolveVariants()}.
   */
  private final Map<String, List<RawVariant>> variantsByTarget = new LinkedHashMap<>();

  /** {@code builder()} attribute value of each target's base (unnamed) registration. */
  private final Map<String, String> builderModeByTarget = new LinkedHashMap<>();

  private static final class RawVariant {
    private final Element declaringElement;
    private final TypeElement source;
    private final String name;
    private final List<String> exclude;

    RawVariant(Element declaringElement, TypeElement source, String name, List<String> exclude) {
      this.declaringElement = declaringElement;
      this.source = source;
      this.name = name;
      this.exclude = exclude;
    }

    Element declaringElement() {
      return declaringElement;
    }

    TypeElement source() {
      return source;
    }

    String name() {
      return name;
    }

    List<String> exclude() {
      return exclude;
    }
  }

  DtoMappingReader(ProcessingContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Register all {@code @DtoMapping} pairs and {@code @DtoMixin} companion types found in this
   * round (called once per processing round - both are typically only present in the first
   * round, but later rounds may still contribute if generated sources declare them too).
   */
  void collect(RoundEnvironment roundEnv) {
    Set<Element> elements = new LinkedHashSet<>();
    addAnnotatedElements(roundEnv, Constants.DTO_MAPPING, elements);
    // DtoMapping is @Repeatable - when used more than once on the same element javac wraps the
    // repeated instances in a single synthetic container annotation (@DtoMapping.List) rather
    // than repeating the plain annotation, so it must also be checked explicitly, and
    // DtoMappingPrism doesn't unwrap that container for us - see allDtoMappingMirrors() below.
    addAnnotatedElements(roundEnv, Constants.DTO_MAPPING_LIST, elements);
    for (Element element : elements) {
      for (AnnotationMirror mirror : allDtoMappingMirrors(element)) {
        DtoMappingPrism prism = DtoMappingPrism.getInstance(mirror);
        if (prism != null) {
          register(element, prism);
        }
      }
    }
    collectMixins(roundEnv);
  }

  /**
   * Discover {@code @DtoMixin(Target.class)} companion types (proactively, via {@code
   * roundEnv.getElementsAnnotatedWith(...)} - unlike {@code @DtoPath}/{@code @DtoRef}/{@code
   * @DtoConvert}, a mixin doesn't annotate an already-being-iterated field of a known {@code
   * @DtoMapping} target, so it can't be discovered lazily from within {@link #resolveProperty}).
   */
  private void collectMixins(RoundEnvironment roundEnv) {
    TypeElement annotationType = ctx.elementUtils().getTypeElement(Constants.DTO_MIXIN);
    if (annotationType == null) {
      return;
    }
    for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
      DtoMixinPrism prism = DtoMixinPrism.getInstanceOn(element);
      if (prism == null) {
        continue;
      }
      if (element.getKind() != ElementKind.INTERFACE && element.getKind() != ElementKind.CLASS) {
        ctx.logError(element, "@DtoMixin must be declared on an interface or class");
        continue;
      }
      TypeElement target = asTypeElement(prism.value());
      if (target == null) {
        ctx.logError(element, "@DtoMixin value() must be a class type");
        continue;
      }
      mixinsByTarget.put(target.getQualifiedName().toString(), (TypeElement) element);
    }
  }

  private void addAnnotatedElements(RoundEnvironment roundEnv, String annotationTypeName, Set<Element> elements) {
    TypeElement annotationType = ctx.elementUtils().getTypeElement(annotationTypeName);
    if (annotationType != null) {
      elements.addAll(roundEnv.getElementsAnnotatedWith(annotationType));
    }
  }

  /**
   * Return the {@code @DtoMapping} annotation mirrors directly present on {@code element},
   * unwrapping the synthetic {@code @DtoMapping.List} container mirror (produced by javac when
   * {@code @DtoMapping} is repeated on the same element) into its individual mirrors.
   */
  private List<AnnotationMirror> allDtoMappingMirrors(Element element) {
    List<AnnotationMirror> result = new ArrayList<>();
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      String fqn = ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
      if (Constants.DTO_MAPPING.equals(fqn)) {
        result.add(mirror);
      } else if (Constants.DTO_MAPPING_LIST.equals(fqn)) {
        for (var entry : mirror.getElementValues().entrySet()) {
          if ("value".contentEquals(entry.getKey().getSimpleName())) {
            @SuppressWarnings("unchecked")
            List<? extends AnnotationValue> nested = (List<? extends AnnotationValue>) entry.getValue().getValue();
            for (AnnotationValue value : nested) {
              result.add((AnnotationMirror) value.getValue());
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Match properties and check for cycles across all pairs collected so far (call once, at the
   * end of processing - i.e. {@code roundEnv.processingOver()}). Returns an empty list if no
   * {@code @DtoMapping} pairs were ever collected.
   */
  List<DtoBeanMeta> resolveAndValidate() {
    if (byTargetName.isEmpty()) {
      return List.of();
    }
    for (DtoBeanMeta meta : byTargetName.values()) {
      resolveProperties(meta);
    }
    resolveVariants();
    for (DtoBeanMeta meta : byTargetName.values()) {
      resolveBuilder(meta, builderModeByTarget.get(meta.targetFullName()));
    }
    List<DtoBeanMeta> result = excludeCycles();
    markNestedElsewhere(result);
    return result;
  }

  /**
   * Attach each raw {@code @DtoMapping(name = ..., exclude = ...)} variant registration (see
   * {@link #register}) to its base mapping's {@link DtoBeanMeta}, validating that a base exists,
   * variant names are unique per target, the variant's source matches the base's source, and
   * each excluded name resolves to a {@code NESTED_ONE}/{@code NESTED_MANY} property.
   */
  private void resolveVariants() {
    for (var entry : variantsByTarget.entrySet()) {
      String targetFqn = entry.getKey();
      DtoBeanMeta meta = byTargetName.get(targetFqn);
      if (meta == null) {
        ctx.logError(entry.getValue().get(0).declaringElement(),
          "@DtoMapping variant(s) declared for target %s but no base @DtoMapping (without name())"
            + " was found - declare one first", targetFqn);
        continue;
      }
      Set<String> seenNames = new LinkedHashSet<>();
      for (RawVariant raw : entry.getValue()) {
        if (!seenNames.add(raw.name())) {
          ctx.logError(raw.declaringElement(), "Duplicate @DtoMapping variant name \"%s\" for target %s",
            raw.name(), targetFqn);
          continue;
        }
        if (raw.source() != meta.source()) {
          ctx.logError(raw.declaringElement(),
            "@DtoMapping(name = \"%s\") variant's source (%s) must match the base mapping's source"
              + " (%s) for target %s", raw.name(), raw.source().getQualifiedName(), meta.sourceFullName(), targetFqn);
          continue;
        }
        Set<String> excluded = resolveExcludedProperties(meta, raw);
        if (excluded != null) {
          meta.addVariant(new DtoMapperVariant(raw.name(), excluded));
        }
      }
    }
  }

  /**
   * Validate and resolve one variant's {@code exclude()} property names against {@code meta}'s
   * already-resolved properties - only {@code NESTED_ONE}/{@code NESTED_MANY} properties can be
   * excluded (there's no type-safe "absent" value for an arbitrary scalar type). Returns
   * {@code null} (with error(s) already logged) if anything doesn't resolve.
   */
  private Set<String> resolveExcludedProperties(DtoBeanMeta meta, RawVariant raw) {
    if (raw.exclude().isEmpty()) {
      ctx.logError(raw.declaringElement(), "@DtoMapping(name = \"%s\") on target %s must exclude() at"
        + " least one nested ToOne/ToMany property", raw.name(), meta.targetFullName());
      return null;
    }
    Set<String> result = new LinkedHashSet<>();
    boolean ok = true;
    for (String propName : raw.exclude()) {
      DtoPropertyMeta property = findProperty(meta, propName);
      if (property == null) {
        ctx.logError(raw.declaringElement(), "@DtoMapping(name = \"%s\") exclude(\"%s\") does not match"
          + " any property on target %s", raw.name(), propName, meta.targetFullName());
        ok = false;
      } else if (property.kind() != DtoPropertyMeta.Kind.NESTED_ONE && property.kind() != DtoPropertyMeta.Kind.NESTED_MANY) {
        ctx.logError(raw.declaringElement(), "@DtoMapping(name = \"%s\") exclude(\"%s\") on target %s is"
          + " not a nested ToOne/ToMany DTO property - only nested properties can be excluded (there's"
          + " no type-safe \"absent\" value for an arbitrary scalar type)", raw.name(), propName, meta.targetFullName());
        ok = false;
      } else {
        result.add(propName);
      }
    }
    return ok ? result : null;
  }

  private DtoPropertyMeta findProperty(DtoBeanMeta meta, String name) {
    for (DtoPropertyMeta property : meta.properties()) {
      if (property.dtoFieldName().equals(name)) {
        return property;
      }
    }
    return null;
  }

  /**
   * Decide (and, if applicable, detect) the builder-based construction path for {@code meta} -
   * see {@code @DtoMapping(builder = ...)} and {@link DtoBuilderMeta}. {@code NEVER} always uses
   * a positional constructor. {@code ALWAYS} requires a matching builder to be found (a codegen
   * error otherwise). {@code AUTO} (the default) silently falls back to a positional constructor
   * if no matching builder is found, and only actually uses one it does find once the target has
   * more than {@link #BUILDER_AUTO_THRESHOLD} properties.
   */
  private void resolveBuilder(DtoBeanMeta meta, String builderMode) {
    String mode = (builderMode == null || builderMode.isEmpty()) ? "AUTO" : builderMode;
    if ("NEVER".equals(mode)) {
      return;
    }
    boolean required = "ALWAYS".equals(mode);
    DtoBuilderMeta builder = detectBuilder(meta, required);
    if (builder != null && (required || meta.properties().size() > BUILDER_AUTO_THRESHOLD)) {
      meta.builderMeta(builder);
    }
  }

  /**
   * Detect an {@code avaje-recordbuilder}-style builder on {@code meta.target()}: a static no-arg
   * {@code builder()} method, whose return type has a no-arg {@code build()} method returning the
   * target type, plus a fluent (returns-itself) same-named setter method for every one of
   * {@code meta}'s properties. Returns {@code null} if any part of that shape is missing - logging
   * a codegen error only when {@code required} (i.e. {@code builder = ALWAYS}); silent otherwise
   * (i.e. {@code builder = AUTO}, where a missing builder just means "use a constructor instead").
   */
  private DtoBuilderMeta detectBuilder(DtoBeanMeta meta, boolean required) {
    TypeElement target = meta.target();
    ExecutableElement builderFactory = findStaticNoArgMethod(target, "builder");
    if (builderFactory == null) {
      if (required) {
        ctx.logError(target, "@DtoMapping(builder = ALWAYS) on target %s but no static no-arg"
          + " builder() method was found", meta.targetFullName());
      }
      return null;
    }
    TypeElement builderType = asTypeElement(builderFactory.getReturnType());
    if (builderType == null) {
      if (required) {
        ctx.logError(target, "@DtoMapping(builder = ALWAYS) on target %s but builder() does not"
          + " return a class type", meta.targetFullName());
      }
      return null;
    }
    ExecutableElement buildMethod = findMethod(builderType, "build");
    if (buildMethod == null || !sameType(buildMethod.getReturnType(), target)) {
      if (required) {
        ctx.logError(target, "@DtoMapping(builder = ALWAYS) on target %s but %s has no build()"
          + " method returning %s", meta.targetFullName(), builderType.getQualifiedName(), meta.targetFullName());
      }
      return null;
    }
    for (DtoPropertyMeta property : meta.properties()) {
      if (findFluentSetter(builderType, property.dtoFieldName()) == null) {
        if (required) {
          ctx.logError(target, "@DtoMapping(builder = ALWAYS) on target %s but %s has no fluent"
            + " \"%s(...)\" method", meta.targetFullName(), builderType.getQualifiedName(), property.dtoFieldName());
        }
        return null;
      }
    }
    return new DtoBuilderMeta(builderType.getQualifiedName().toString());
  }

  private ExecutableElement findStaticNoArgMethod(TypeElement type, String name) {
    for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
      if (method.getSimpleName().contentEquals(name) && method.getModifiers().contains(Modifier.STATIC)
        && method.getParameters().isEmpty()) {
        return method;
      }
    }
    return null;
  }

  private ExecutableElement findFluentSetter(TypeElement builderType, String propertyName) {
    for (ExecutableElement method : ElementFilter.methodsIn(builderType.getEnclosedElements())) {
      if (method.getSimpleName().contentEquals(propertyName) && method.getParameters().size() == 1
        && sameType(method.getReturnType(), builderType)) {
        return method;
      }
    }
    return null;
  }

  private boolean sameType(TypeMirror mirror, TypeElement expected) {
    TypeElement actual = asTypeElement(mirror);
    return actual != null && actual.getQualifiedName().contentEquals(expected.getQualifiedName());
  }

  /**
   * Mark every {@link DtoBeanMeta} that's used as a {@code NESTED_ONE}/{@code NESTED_MANY}
   * property by some other mapper in {@code metas} - see {@link DtoBeanMeta#nestedElsewhere()}.
   */
  private void markNestedElsewhere(List<DtoBeanMeta> metas) {
    for (DtoBeanMeta meta : metas) {
      for (DtoPropertyMeta property : meta.properties()) {
        DtoBeanMeta nested = property.nested();
        if (nested != null) {
          nested.markNestedElsewhere();
        }
      }
    }
  }

  private void register(Element declaringElement, DtoMappingPrism prism) {
    TypeElement sourceType = asTypeElement(prism.source());
    TypeElement targetType = asTypeElement(prism.target());
    if (sourceType == null || targetType == null) {
      ctx.logError(declaringElement, "@DtoMapping source/target must be class types");
      return;
    }
    String targetFqn = targetType.getQualifiedName().toString();
    String name = prism.name() == null ? "" : prism.name().trim();
    if (name.isEmpty()) {
      if (byTargetName.containsKey(targetFqn)) {
        ctx.logError(declaringElement, "Duplicate base @DtoMapping for target %s - only one base"
          + " (unnamed) mapping is allowed per target, use name() for additional variants", targetFqn);
        return;
      }
      String mapperPackage = resolveMapperPackage(prism, targetType, declaringElement);
      byTargetName.put(targetFqn, new DtoBeanMeta(sourceType, targetType, mapperPackage));
      builderModeByTarget.put(targetFqn, prism.builder());
    } else {
      variantsByTarget.computeIfAbsent(targetFqn, t -> new ArrayList<>())
        .add(new RawVariant(declaringElement, sourceType, name, prism.exclude()));
    }
  }

  private TypeElement asTypeElement(TypeMirror mirror) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return null;
    }
    return (TypeElement) ((DeclaredType) mirror).asElement();
  }

  /**
   * Resolve the package the generated mapper for {@code target} lives in: an explicit
   * {@code mapperPackage()} override always wins; otherwise the target DTO's own package is
   * reused, unless the DTO belongs to a different (named) Java module than the one declaring
   * the {@code @DtoMapping}, in which case reusing it would be a JPMS split-package violation -
   * in that case a package derived from the declaring module's own name is used instead
   * (mirroring {@code LookupWriter}'s module-name-derived package fallback).
   */
  private String resolveMapperPackage(DtoMappingPrism prism, TypeElement target, Element declaringElement) {
    String override = prism.mapperPackage();
    if (override != null && !override.isEmpty()) {
      return override;
    }
    ModuleElement targetModule = ctx.elementUtils().getModuleOf(target);
    ModuleElement declaringModule = ctx.elementUtils().getModuleOf(declaringElement);
    if (targetModule == null || declaringModule == null
      || targetModule.isUnnamed() || declaringModule.isUnnamed()
      || targetModule.getQualifiedName().contentEquals(declaringModule.getQualifiedName())) {
      return Split.split(target.getQualifiedName().toString())[0];
    }
    return declaringModule.getQualifiedName() + ".dto";
  }

  private void resolveProperties(DtoBeanMeta meta) {
    for (VariableElement field : ElementFilter.fieldsIn(meta.target().getEnclosedElements())) {
      if (field.getModifiers().contains(Modifier.STATIC)) {
        continue;
      }
      meta.addProperty(resolveProperty(field, meta));
    }
  }

  private DtoPropertyMeta resolveProperty(VariableElement field, DtoBeanMeta meta) {
    String name = field.getSimpleName().toString();
    DtoConverterMeta converter = resolveConverter(field, meta);
    DtoRefPrism refPrism = prismOn(field, meta, DtoRefPrism::getInstanceOn);
    if (refPrism != null) {
      String assocName = (name.endsWith("Id") && name.length() > 2) ? name.substring(0, name.length() - 2) : name;
      String assocGetter = getterName(meta.source(), assocName);
      TypeElement assocType = getterReturnType(meta.source(), assocGetter);
      String idGetter = getterName(assocType, "id");
      // @DtoRef has no failOnNull escape hatch - always default to the primitive's zero-equivalent
      // rather than let a null-guarded getter chain auto-unbox to a NullPointerException.
      return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.REF, List.of(assocGetter, idGetter), List.of(assocName, "id"),
        null, converter, field.asType().getKind().isPrimitive(), false);
    }
    DtoPathPrism pathPrism = prismOn(field, meta, DtoPathPrism::getInstanceOn);
    if (pathPrism != null) {
      List<String> getters = new ArrayList<>();
      List<String> properties = new ArrayList<>();
      TypeElement currentType = meta.source();
      TypeElement computedDeclaringType = null;
      int computedFrom = -1;
      for (String segment : pathPrism.value().split("\\.")) {
        String getter = getterName(currentType, segment);
        getters.add(getter);
        properties.add(segment);
        if (computedFrom < 0 && (currentType == null || !hasField(currentType, segment))) {
          computedFrom = properties.size() - 1;
          computedDeclaringType = currentType;
        }
        currentType = currentType != null ? getterReturnType(currentType, getter) : null;
      }
      // a single-hop @DtoPath rename (e.g. @DtoPath("eboxStatus") on a field named "status")
      // can still target a type with its own registered @DtoMapping - detect that the same way
      // the plain (non-@DtoPath) branches below do, rather than always falling back to a raw
      // scalar getter call that would fail to compile with a type mismatch against the nested
      // DTO type. Multi-hop paths keep the existing scalar/flattening behaviour since fetch spec
      // derivation for NESTED_ONE/MANY only supports a single association name.
      if (properties.size() == 1) {
        TypeMirror fieldType = field.asType();
        TypeMirror listElementType = listElementType(fieldType);
        if (listElementType != null) {
          DtoBeanMeta nested = lookupByTarget(listElementType);
          if (nested != null) {
            return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.NESTED_MANY, getters, properties, nested);
          }
        } else {
          DtoBeanMeta nested = lookupByTarget(fieldType);
          if (nested != null) {
            return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.NESTED_ONE, getters, properties, nested);
          }
        }
      }
      List<String> requiredFetchPaths = List.of();
      if (computedFrom >= 0) {
        // a segment with no backing field is a computed/derived getter, not a real, fetchable
        // Ebean property - its own data dependencies (what it touches internally) can't be
        // inferred from the path alone, so require the developer to spell them out explicitly
        // via @DtoPath(requires = {...}) rather than silently generating a FetchGroup.fetch(...)
        // call for a path segment Ebean doesn't actually recognise (which only fails at runtime).
        String realPrefix = computedFrom == 0 ? null : String.join(".", properties.subList(0, computedFrom));
        // pathPrism.requires() always returns List.of() whether the attribute was explicitly
        // written as an empty array or omitted entirely - only pathPrism.values.requires() (which
        // returns null for a defaulted/omitted member) can tell the two apart. That distinction
        // matters here: an explicit requires = {} is the developer's way of confirming the
        // computed getter genuinely needs nothing extra fetched, whereas omitting requires
        // entirely means they haven't considered it yet - only the latter should fail the build.
        if (pathPrism.values.requires() == null) {
          String hint = realPrefix != null
            ? String.format(" (e.g. requires = \"%s\", or a deeper path under it your getter actually needs)", realPrefix)
            : "";
          ctx.logError(field,
            "@DtoPath(\"%s\") on %s traverses '%s' which has no backing field on %s - it looks like"
              + " a computed/derived getter rather than a real, fetchable Ebean property, so its data"
              + " dependencies can't be inferred automatically. Specify @DtoPath(requires = {...})"
              + " naming the real entity paths that must be fetched for it to execute safely%s, or"
              + " requires = {} if it genuinely needs nothing extra fetched, or remove @DtoPath and"
              + " compute this value another way (e.g. @DtoConvert).",
            pathPrism.value(), meta.targetFullName(), properties.get(computedFrom),
            computedDeclaringType != null ? computedDeclaringType.getSimpleName() : "?", hint);
        }
        List<String> combined = new ArrayList<>();
        if (realPrefix != null) {
          combined.add(realPrefix);
        }
        combined.addAll(pathPrism.requires());
        requiredFetchPaths = combined;
      }
      // A multi-hop path can pass through a nullable intermediate relation - if the DTO field is
      // primitive, the generated null-guarded getter chain would otherwise auto-unbox a null
      // straight into a NullPointerException. Default to the primitive's zero-equivalent value,
      // or fail fast with a clear message instead when @DtoPath(failOnNull = true).
      return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.SCALAR, getters, properties, null, converter,
        field.asType().getKind().isPrimitive(), pathPrism.failOnNull(), computedFrom >= 0, requiredFetchPaths);
    }
    TypeMirror fieldType = field.asType();
    TypeMirror listElementType = listElementType(fieldType);
    if (listElementType != null) {
      DtoBeanMeta nested = lookupByTarget(listElementType);
      if (nested != null) {
        return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.NESTED_MANY, List.of(getterName(meta.source(), name)), List.of(name), nested);
      }
    } else {
      DtoBeanMeta nested = lookupByTarget(fieldType);
      if (nested != null) {
        return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.NESTED_ONE, List.of(getterName(meta.source(), name)), List.of(name), nested);
      }
    }
    return new DtoPropertyMeta(name, DtoPropertyMeta.Kind.SCALAR, List.of(getterName(meta.source(), name)), List.of(name), null, converter);
  }

  /**
   * Resolve a prism instance for {@code field}, falling back to the corresponding
   * {@code @DtoMixin} companion method (same simple name) when {@code field} carries no such
   * annotation directly and {@code meta.target()} has a registered mixin - this is what lets
   * {@code @DtoPath}/{@code @DtoRef}/{@code @DtoConvert} be declared on a mixin instead of the
   * (possibly generated/unowned) DTO type itself.
   */
  private <A> A prismOn(VariableElement field, DtoBeanMeta meta, java.util.function.Function<Element, A> lookup) {
    A direct = lookup.apply(field);
    if (direct != null) {
      return direct;
    }
    ExecutableElement mixinMethod = findMixinMethod(meta, field.getSimpleName().toString());
    return mixinMethod == null ? null : lookup.apply(mixinMethod);
  }

  /**
   * The method on {@code meta.target()}'s registered {@code @DtoMixin} companion (if any) whose
   * name matches {@code propertyName}, or {@code null} if no mixin is registered for this target
   * or it declares no matching method.
   */
  private ExecutableElement findMixinMethod(DtoBeanMeta meta, String propertyName) {
    TypeElement mixin = mixinsByTarget.get(meta.target().getQualifiedName().toString());
    return mixin == null ? null : findMethod(mixin, propertyName);
  }

  /**
   * Resolve the {@code @DtoConvert} conversion declared on {@code field} (or, failing that, its
   * {@code @DtoMixin} companion method - see {@link #prismOn}), if any - determining
   * static-vs-instance dispatch by locating the named method on the converter type. Returns
   * {@code null} (with no error) if {@code @DtoConvert} isn't present at all.
   */
  private DtoConverterMeta resolveConverter(VariableElement field, DtoBeanMeta meta) {
    DtoConvertPrism prism = prismOn(field, meta, DtoConvertPrism::getInstanceOn);
    if (prism == null) {
      return null;
    }
    TypeElement converterType = asTypeElement(prism.value());
    if (converterType == null) {
      ctx.logError(field, "@DtoConvert value() must be a class type");
      return null;
    }
    String methodName = prism.method();
    ExecutableElement method = findMethod(converterType, methodName);
    if (method == null) {
      ctx.logError(field, "@DtoConvert method \"%s\" not found on %s", methodName, converterType.getQualifiedName());
      return null;
    }
    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
    return new DtoConverterMeta(converterType.getQualifiedName().toString(), methodName, isStatic);
  }

  private ExecutableElement findMethod(TypeElement type, String methodName) {
    for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
      if (method.getSimpleName().contentEquals(methodName)) {
        return method;
      }
    }
    return null;
  }

  private DtoBeanMeta lookupByTarget(TypeMirror type) {
    TypeElement element = asTypeElement(type);
    return element == null ? null : byTargetName.get(element.getQualifiedName().toString());
  }

  /**
   * Return the {@code java.util.List} element type of {@code type}, or {@code null} if it isn't
   * a (raw or parameterized) {@code java.util.List}.
   */
  private TypeMirror listElementType(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declaredType = (DeclaredType) type;
    TypeElement typeElement = (TypeElement) declaredType.asElement();
    if (!typeElement.getQualifiedName().contentEquals("java.util.List")) {
      return null;
    }
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    return typeArguments.isEmpty() ? null : typeArguments.get(0);
  }

  /**
   * Resolve the accessor method name for {@code propertyName} on {@code type} (searching
   * {@code type} and its superclass chain) by checking, in order, which shape actually exists as
   * a real no-arg method: (1) {@code isXxx()} returning {@code boolean} (standard JavaBean
   * convention for a primitive boolean accessor), (2) {@code getXxx()} (standard JavaBean
   * convention), (3) the bare {@code propertyName()} itself - a "record-style" accessor, which
   * isn't limited to an actual {@code record} type (Ebean does support record entity beans, but
   * an ordinary class can just as easily expose bare/fluent-style accessors with no {@code get}/
   * {@code is} prefix at all). Falls back to the guessed {@code getXxx()} name if {@code type} is
   * {@code null} (unresolvable, e.g. a later {@code @DtoPath} segment whose receiver type
   * couldn't be determined) or none of the three shapes actually exist.
   */
  private String getterName(TypeElement type, String propertyName) {
    String capitalized = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    String isName = "is" + capitalized;
    String getName = "get" + capitalized;
    if (type == null) {
      return getName;
    }
    if (hasNoArgMethod(type, isName, TypeKind.BOOLEAN)) {
      return isName;
    }
    if (hasNoArgMethod(type, getName, null)) {
      return getName;
    }
    if (hasNoArgMethod(type, propertyName, null)) {
      return propertyName;
    }
    return getName;
  }

  /**
   * Whether {@code type} (searching {@code type} and its superclass chain) declares a field
   * named {@code propertyName} - used to distinguish a real, fetchable Ebean bean property (which
   * always has a backing field once enhanced) from a computed/derived getter with no backing
   * storage at all (e.g. a hand-written method that filters/derives a value from other
   * properties). {@code type == null} (unresolvable) conservatively returns {@code true} so an
   * already-unresolvable segment doesn't also get flagged as "computed" - it'll already have
   * fallen back to a guessed getter name via {@link #getterName}.
   */
  private boolean hasField(TypeElement type, String propertyName) {
    if (type == null) {
      return true;
    }
    for (TypeElement current = type; current != null; current = superclassOf(current)) {
      for (VariableElement f : ElementFilter.fieldsIn(current.getEnclosedElements())) {
        if (f.getSimpleName().contentEquals(propertyName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Whether a no-arg method named {@code methodName} exists on {@code type} (searching
   * {@code type} and its superclass chain), optionally constrained to a specific return
   * {@code TypeKind} (e.g. {@code TypeKind.BOOLEAN} for a primitive boolean accessor) - pass
   * {@code null} to accept any return type.
   */
  private boolean hasNoArgMethod(TypeElement type, String methodName, TypeKind returnKind) {
    for (TypeElement current = type; current != null; current = superclassOf(current)) {
      for (ExecutableElement method : ElementFilter.methodsIn(current.getEnclosedElements())) {
        if (method.getParameters().isEmpty() && method.getSimpleName().contentEquals(methodName)
          && (returnKind == null || method.getReturnType().getKind() == returnKind)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * The return type of the no-arg method {@code getterMethodName} resolved on {@code type}
   * (searching {@code type} and its superclass chain) - used to walk each successive segment of
   * a multi-segment {@code @DtoPath} through the actual intermediate types, so each segment's
   * own accessor name can be resolved correctly against its real receiver type (e.g. a
   * {@code boolean} property partway through the path). Returns {@code null} if unresolvable, in
   * which case later segments fall back to the guessed {@code getXxx()} name.
   */
  private TypeElement getterReturnType(TypeElement type, String getterMethodName) {
    for (TypeElement current = type; current != null; current = superclassOf(current)) {
      for (ExecutableElement method : ElementFilter.methodsIn(current.getEnclosedElements())) {
        if (method.getParameters().isEmpty() && method.getSimpleName().contentEquals(getterMethodName)) {
          return asTypeElement(method.getReturnType());
        }
      }
    }
    return null;
  }

  private TypeElement superclassOf(TypeElement type) {
    return asTypeElement(type.getSuperclass());
  }

  /**
   * DFS-based cycle detection over the nested-DTO type graph (edges are {@code NESTED_ONE}/
   * {@code NESTED_MANY} properties only - {@code REF}/{@code SCALAR} properties don't recurse
   * into another mapper, so they can't participate in a cycle). Any {@link DtoBeanMeta} that
   * takes part in a detected cycle is logged as an error and excluded from the returned list.
   */
  private List<DtoBeanMeta> excludeCycles() {
    List<DtoBeanMeta> result = new ArrayList<>();
    java.util.Set<DtoBeanMeta> cyclic = new java.util.HashSet<>();
    for (DtoBeanMeta meta : byTargetName.values()) {
      if (meta.visit == DtoBeanMeta.Visit.WHITE) {
        detectCycle(meta, new ArrayDeque<>(), cyclic);
      }
    }
    for (DtoBeanMeta meta : byTargetName.values()) {
      if (!cyclic.contains(meta)) {
        result.add(meta);
      }
    }
    return result;
  }

  private void detectCycle(DtoBeanMeta meta, Deque<DtoBeanMeta> path, java.util.Set<DtoBeanMeta> cyclic) {
    meta.visit = DtoBeanMeta.Visit.GRAY;
    path.push(meta);
    for (DtoPropertyMeta property : meta.properties()) {
      DtoBeanMeta nested = property.nested();
      if (nested == null) {
        continue;
      }
      if (nested.visit == DtoBeanMeta.Visit.GRAY) {
        reportCycle(path, nested, cyclic);
      } else if (nested.visit == DtoBeanMeta.Visit.WHITE) {
        detectCycle(nested, path, cyclic);
      }
    }
    path.pop();
    meta.visit = DtoBeanMeta.Visit.BLACK;
  }

  private void reportCycle(Deque<DtoBeanMeta> path, DtoBeanMeta cycleStart, java.util.Set<DtoBeanMeta> cyclic) {
    StringBuilder chain = new StringBuilder(cycleStart.targetFullName());
    for (DtoBeanMeta onPath : path) {
      chain.insert(0, onPath.targetFullName() + " -> ");
      cyclic.add(onPath);
      if (onPath == cycleStart) {
        break;
      }
    }
    cyclic.add(cycleStart);
    ctx.logError(cycleStart.target(),
      "@DtoMapping cycle detected, not generating mappers for: %s -> %s "
        + "(introduce a separate shallow DTO type, or an @DtoRef id-only field, to break the cycle)",
      chain, cycleStart.targetFullName());
  }
}
