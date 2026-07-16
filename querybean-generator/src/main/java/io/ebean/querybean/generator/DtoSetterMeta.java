package io.ebean.querybean.generator;

/**
 * Marks a detected setter-based (mutable JavaBean) construction path for a {@link DtoBeanMeta}'s
 * target - see {@code @DtoMapping(setter = ...)}.
 * <p>
 * Detected via the plain JavaBean convention (the shape JAXB/XSD-generated legacy SOAP types
 * commonly follow): a public no-arg constructor plus a public {@code setXxx(propertyType)} setter
 * for every one of the target's mapped properties - either {@code void} or fluent-style
 * (returning the target type itself, e.g. {@code public Target setXxx(...) { ...; return this; }})
 * are both accepted, since the generated code always calls the setter as a bare statement and
 * discards any return value. When present (and either explicitly
 * requested via {@code setter = ALWAYS}, or no builder was selected and the target has no
 * positional constructor matching the mapped properties under {@code setter = AUTO}), the
 * generated mapper constructs the target via {@code Target target = new Target();
 * target.setX(...); ...; return target;} instead of a positional constructor call or a builder
 * chain.
 * <p>
 * A pure marker (mirrors {@link DtoBuilderMeta}) - unlike a builder, a setter-constructed target
 * needs no separate intermediate type name, since the setters are called directly on the target
 * type itself.
 * <p>
 * Deliberately no {@code mapToBuilder(...)}-style post-construction accessor is generated for this
 * strategy: the returned target is already the final, fully mutable instance (its setters are
 * required to be {@code public} - see above), so a caller can already override an
 * {@code @DtoIgnore}/derived property directly, e.g.
 * {@code Ebox ebox = mapper.map(source); ebox.setMachineSummaryInfo(loadSummary(source));} -
 * exactly the pattern already used by hand-written mappers like central-access's
 * {@code EboxMapper}. This is unlike the builder strategy, where the intermediate builder object
 * is distinct from (and otherwise inaccessible after) the final, one-shot-{@code build()}ed,
 * often-immutable target - {@code mapToBuilder(...)} exists there specifically to expose that
 * otherwise-unreachable intermediate step.
 */
final class DtoSetterMeta {
}
