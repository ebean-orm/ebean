package io.ebean.querybean.generator;

/**
 * Metadata for a detected builder-style construction path for a {@link DtoBeanMeta}'s target -
 * see {@code @DtoMapping(builder = ...)}.
 * <p>
 * Detected via the {@code avaje-recordbuilder}-style convention: a static no-arg
 * {@code Target.builder()} factory method returning some builder type, which in turn has a
 * fluent (returns-itself), same-named setter method per target property, plus a no-arg
 * {@code build()} method returning the target type. When present (and either explicitly
 * requested via {@code builder = ALWAYS}, or the target has more properties than the
 * auto-detection threshold under {@code builder = AUTO}), the generated mapper constructs the
 * target via {@code Target.builder().prop(x)....build()} instead of a positional constructor
 * call.
 */
final class DtoBuilderMeta {

  private final String builderTypeFullName;

  DtoBuilderMeta(String builderTypeFullName) {
    this.builderTypeFullName = builderTypeFullName;
  }

  String builderTypeFullName() {
    return builderTypeFullName;
  }

  String builderTypeShortName() {
    return Split.shortName(builderTypeFullName);
  }
}
