package io.ebean.querybean.generator;

import java.util.List;

/**
 * Metadata for a single target DTO property (constructor argument), describing how its value is
 * obtained from the source entity.
 */
class DtoPropertyMeta {

  enum Kind {
    /** Direct (or {@code @DtoPath}) scalar getter chain on the source, e.g. {@code s.getName()}. */
    SCALAR,
    /** {@code @DtoRef} id-only shortcut, e.g. {@code s.getCustomer().getId()}. */
    REF,
    /** Nested ToOne - delegates to another generated mapper. */
    NESTED_ONE,
    /** Nested ToMany - delegates to another generated mapper's {@code mapList}. */
    NESTED_MANY
  }

  private final String dtoFieldName;
  private final Kind kind;
  private final List<String> sourceGetterPath;
  private final List<String> sourcePropertyPath;
  private final DtoBeanMeta nested;
  private final DtoConverterMeta converter;
  private final boolean primitiveTarget;
  private final boolean failOnNull;
  private final boolean computedSegment;
  private final List<String> requiredFetchPaths;
  private final boolean listTarget;
  private final boolean ignored;

  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath, DtoBeanMeta nested) {
    this(dtoFieldName, kind, sourceGetterPath, sourcePropertyPath, nested, null);
  }

  /**
   * {@code @DtoIgnore} factory - a property permanently excluded from every mapping (base and
   * every named variant alike), always given its empty default rather than resolved from any
   * source getter/path at all - see {@code DtoIgnore}'s javadoc. Kept as {@link Kind#SCALAR} with
   * empty getter/property paths since {@link DtoMapperWriter} short-circuits on {@link #isIgnored()}
   * before ever consulting them.
   */
  static DtoPropertyMeta ignored(String dtoFieldName, boolean listTarget) {
    return new DtoPropertyMeta(dtoFieldName, Kind.SCALAR, List.of(), List.of(), null, null, false, false, false, List.of(), listTarget, true);
  }

  /**
   * {@code NESTED_ONE}/{@code NESTED_MANY} constructor variant for a single-hop {@code @DtoPath}
   * rename that traverses a computed/derived getter segment (no backing field) - see
   * {@link #hasComputedSegment()}. Just as unfetchable via {@code FetchGroup.fetch(path, ...)} as
   * the analogous {@link Kind#SCALAR} case, so it carries the same
   * {@code computedSegment}/{@code requiredFetchPaths} through to {@code DtoMapperWriter}.
   */
  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath,
                  DtoBeanMeta nested, boolean computedSegment, List<String> requiredFetchPaths) {
    this(dtoFieldName, kind, sourceGetterPath, sourcePropertyPath, nested, null, false, false, computedSegment, requiredFetchPaths, false, false);
  }

  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath, DtoBeanMeta nested, DtoConverterMeta converter) {
    this(dtoFieldName, kind, sourceGetterPath, sourcePropertyPath, nested, converter, false, false);
  }

  /**
   * Plain (no {@code @DtoPath}) {@link Kind#SCALAR} constructor variant that also records whether
   * the DTO field type is a {@code java.util.List} - see {@code listTarget} on the full
   * constructor.
   */
  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath,
                  DtoBeanMeta nested, DtoConverterMeta converter, boolean primitiveTarget, boolean listTarget) {
    this(dtoFieldName, kind, sourceGetterPath, sourcePropertyPath, nested, converter, primitiveTarget, false, false, List.of(), listTarget, false);
  }

  /**
   * Full constructor - {@code primitiveTarget}/{@code failOnNull} only matter for a multi-hop
   * ({@code sourceGetterPath.size() > 1}) {@link Kind#SCALAR}/{@link Kind#REF} property whose DTO
   * field type is a Java primitive, per {@code @DtoPath#failOnNull()} - see
   * {@link #sourceValueExpression(String)}. {@code computedSegment} is {@code true} only for a
   * {@code @DtoPath} that traverses a computed/derived getter segment (no backing field) - see
   * {@link #hasComputedSegment()}; kept separate from whether {@code requiredFetchPaths} happens
   * to be empty, since {@code @DtoPath(requires = {})} legitimately declares "nothing extra
   * needed" for a computed segment. {@code listTarget} is {@code true} when this property's DTO
   * field type is a {@code java.util.List} - relevant only for {@link Kind#SCALAR} (e.g. a
   * {@code @DtoConvert}-backed {@code List} property with no registered nested DTO mapping of its
   * own, like a fleet list populated from ad-hoc SQL) - see
   * {@code DtoMapperWriter#defaultValueFor}.
   */
  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath,
                  DtoBeanMeta nested, DtoConverterMeta converter, boolean primitiveTarget, boolean failOnNull,
                  boolean computedSegment, List<String> requiredFetchPaths, boolean listTarget, boolean ignored) {
    this.dtoFieldName = dtoFieldName;
    this.kind = kind;
    this.sourceGetterPath = sourceGetterPath;
    this.sourcePropertyPath = sourcePropertyPath;
    this.nested = nested;
    this.converter = converter;
    this.primitiveTarget = primitiveTarget;
    this.failOnNull = failOnNull;
    this.computedSegment = computedSegment;
    this.requiredFetchPaths = requiredFetchPaths;
    this.listTarget = listTarget;
    this.ignored = ignored;
  }

  String dtoFieldName() {
    return dtoFieldName;
  }

  Kind kind() {
    return kind;
  }

  List<String> sourceGetterPath() {
    return sourceGetterPath;
  }

  /**
   * The literal Ebean bean-property name for each hop of {@link #sourceGetterPath()} (e.g.
   * {@code ["billingAddress", "line1"]}) - kept separately from the getter-call path since the
   * two diverge for accessor styles other than a plain {@code getXxx()}/{@code isXxx()} JavaBean
   * getter (record-style bare accessors, boolean {@code isXxx()} accessors). Used to build
   * {@code FetchGroup.select(...)}/{@code .fetch(...)} property strings without having to
   * reverse-parse an accessor method name back into a property name.
   */
  List<String> sourcePropertyPath() {
    return sourcePropertyPath;
  }

  /**
   * The other {@link DtoBeanMeta} this property delegates to, for {@link Kind#NESTED_ONE} and
   * {@link Kind#NESTED_MANY} properties. {@code null} otherwise.
   */
  DtoBeanMeta nested() {
    return nested;
  }

  /**
   * The {@code @DtoConvert} conversion to apply to this property's raw source value, or
   * {@code null} if this property is mapped directly with no conversion.
   */
  DtoConverterMeta converter() {
    return converter;
  }

  /**
   * {@code true} if this {@code @DtoPath} traverses a segment with no backing field (a computed/
   * derived getter rather than a real, fetchable Ebean property) - in which case
   * {@link #sourcePropertyPath()} must NOT be used to derive a {@code .fetch(path, "props")}
   * ({@link Kind#SCALAR}) or {@code .fetch(path, mapper.fetchGroup())} ({@link Kind#NESTED_ONE}/
   * {@link Kind#NESTED_MANY}) call (the path isn't a real Ebean fetch path), and
   * {@link #requiredFetchPaths()} should be used instead (see {@code @DtoPath#requires()}).
   */
  boolean hasComputedSegment() {
    return computedSegment;
  }

  /**
   * {@code true} when this property's DTO field type is a Java primitive - a primitive property
   * has no type-safe "absent" value, so can never be excluded by a named
   * {@code @DtoMapping(name = ..., exclude = ...)} variant.
   */
  boolean isPrimitiveTarget() {
    return primitiveTarget;
  }

  /**
   * {@code true} when this property's DTO field type is a {@code java.util.List} - set for
   * {@link Kind#NESTED_MANY} (always list-shaped) and also for a {@link Kind#SCALAR}/
   * {@code @DtoConvert}-backed property whose DTO field happens to be a {@code List} with no
   * registered nested DTO mapping of its own (e.g. a fleet list populated from ad-hoc SQL) -
   * used to pick {@code List.of()} rather than {@code null} as the excluded/empty default for a
   * named variant, see {@code DtoMapperWriter#defaultValueFor}.
   */
  boolean isListTarget() {
    return listTarget || kind == Kind.NESTED_MANY;
  }

  /**
   * {@code true} when this property is marked {@code @DtoIgnore} - permanently excluded from
   * every mapping (base and every named variant alike), always given its empty default rather
   * than resolved from any source getter/path.
   */
  boolean isIgnored() {
    return ignored;
  }

  /**
   * Real entity paths that must be added to the {@code FetchGroup} to support this property's
   * computed/derived getter segment - the real prefix path (if any) followed by the declared
   * {@code @DtoPath#requires()} paths. Empty by default when {@link #hasComputedSegment()} is
   * {@code false}; can also legitimately be empty when it's {@code true} (an explicit
   * {@code @DtoPath(requires = {})} confirming nothing extra is needed).
   */
  List<String> requiredFetchPaths() {
    return requiredFetchPaths;
  }

  /**
   * Return a source expression chaining {@link #sourceGetterPath()} getters off the given root
   * variable. A single getter is a plain call, e.g. {@code s.getName()}; a multi-hop chain (from
   * {@code @DtoPath} or {@code @DtoRef}) null-guards each intermediate hop, e.g.
   * {@code (s.getBillingAddress() == null ? null : s.getBillingAddress().getLine1())}.
   * <p>
   * That null-guarded chain always types as the boxed wrapper (one ternary branch is the
   * {@code null} literal) - when {@link #primitiveTarget} is set (the DTO field is a Java
   * primitive), the whole chain is additionally wrapped in a {@code DtoMapperSupport} call so it
   * safely resolves to the primitive's zero-equivalent value (the default), or throws a clear
   * exception instead, per {@code @DtoPath#failOnNull()} - see {@code DtoMapperSupport}.
   */
  String sourceValueExpression(String rootVariable) {
    if (sourceGetterPath.size() == 1) {
      return rootVariable + "." + sourceGetterPath.get(0) + "()";
    }
    StringBuilder sb = new StringBuilder();
    appendGuardedChain(sb, rootVariable, 0);
    String chain = sb.toString();
    if (!primitiveTarget) {
      return chain;
    }
    return failOnNull
      ? "DtoMapperSupport.require(" + chain + ", \"" + String.join(".", sourcePropertyPath) + "\")"
      : "DtoMapperSupport.orZero(" + chain + ")";
  }

  /**
   * {@code true} if {@link #sourceValueExpression(String)} wraps its chain in a
   * {@code DtoMapperSupport} call - i.e. this is a multi-hop {@link Kind#SCALAR}/{@link Kind#REF}
   * property whose DTO field type is primitive. Used to conditionally import
   * {@code io.ebean.DtoMapperSupport} only when actually referenced.
   */
  boolean usesMapperSupport() {
    return primitiveTarget && sourceGetterPath.size() > 1;
  }

  private void appendGuardedChain(StringBuilder sb, String prefix, int index) {
    if (index == sourceGetterPath.size() - 1) {
      sb.append(prefix).append('.').append(sourceGetterPath.get(index)).append("()");
      return;
    }
    String nextPrefix = prefix + "." + sourceGetterPath.get(index) + "()";
    sb.append('(').append(nextPrefix).append(" == null ? null : ");
    appendGuardedChain(sb, nextPrefix, index + 1);
    sb.append(')');
  }
}

