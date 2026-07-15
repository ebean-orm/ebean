package io.ebean.querybean.generator;

import java.util.Set;

/**
 * A named variant of a {@link DtoBeanMeta} - see {@code @DtoMapping(name = "...", exclude =
 * "...")}. Generated into the *same* mapper class as the base mapping (not a separate class),
 * excluding one or more nested {@code ToOne}/{@code ToMany} properties from both its own
 * {@code fetchGroup} and its mapped output.
 * <p>
 * Exposed on the generated mapper as a same-named accessor method returning its own
 * {@code DtoMapper<SOURCE, TARGET>} view (a small inner class delegating back to the shared,
 * once-written {@code build(...)} method) - e.g. {@code name = "noFleets"} generates a
 * {@code noFleets()} method, so callers can do
 * {@code query.mapTo(Target.class, mapper.noFleets())} with no string-based lookup.
 */
final class DtoMapperVariant {

  private final String name;
  private final String suffix;
  private final Set<String> excludedProperties;

  DtoMapperVariant(String name, Set<String> excludedProperties) {
    this.name = name;
    this.suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    this.excludedProperties = excludedProperties;
  }

  /** The variant name as declared, e.g. {@code "noFleets"} - also the accessor method name. */
  String name() {
    return name;
  }

  /** {@link #name()} capitalized, e.g. {@code "NoFleets"} - used to derive generated identifiers. */
  String suffix() {
    return suffix;
  }

  /** Dto field names (of {@code NESTED_ONE}/{@code NESTED_MANY} properties) this variant excludes. */
  Set<String> excludedProperties() {
    return excludedProperties;
  }

  boolean excludes(DtoPropertyMeta property) {
    return excludedProperties.contains(property.dtoFieldName());
  }

  /** Field name holding this variant's own {@code FetchGroup}, e.g. {@code fetchGroupNoFleets}. */
  String fetchGroupFieldName() {
    return "fetchGroup" + suffix;
  }

  /** Inner class name implementing {@code DtoMapper<SOURCE, TARGET>} for this variant. */
  String innerClassName() {
    return suffix + "Mapper";
  }

  /** Field name holding the (lazily unnecessary - eagerly built) inner mapper instance. */
  String fieldName() {
    return name + "View";
  }
}
