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

  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath, DtoBeanMeta nested) {
    this(dtoFieldName, kind, sourceGetterPath, sourcePropertyPath, nested, null);
  }

  DtoPropertyMeta(String dtoFieldName, Kind kind, List<String> sourceGetterPath, List<String> sourcePropertyPath, DtoBeanMeta nested, DtoConverterMeta converter) {
    this.dtoFieldName = dtoFieldName;
    this.kind = kind;
    this.sourceGetterPath = sourceGetterPath;
    this.sourcePropertyPath = sourcePropertyPath;
    this.nested = nested;
    this.converter = converter;
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
   * Return a source expression chaining {@link #sourceGetterPath()} getters off the given root
   * variable. A single getter is a plain call, e.g. {@code s.getName()}; a multi-hop chain (from
   * {@code @DtoPath} or {@code @DtoRef}) null-guards each intermediate hop, e.g.
   * {@code (s.getBillingAddress() == null ? null : s.getBillingAddress().getLine1())}.
   */
  String sourceValueExpression(String rootVariable) {
    if (sourceGetterPath.size() == 1) {
      return rootVariable + "." + sourceGetterPath.get(0) + "()";
    }
    StringBuilder sb = new StringBuilder();
    appendGuardedChain(sb, rootVariable, 0);
    return sb.toString();
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
