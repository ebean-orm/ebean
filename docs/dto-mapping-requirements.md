# Nested DTO Mapping — Requirements

Design requirements distilled from [issue #2540 "Support nested DTO mapping"](https://github.com/ebean-orm/ebean/issues/2540),
reviewed against comparable features in QueryDSL (`@QueryProjection`) and Blaze-Persistence (`@EntityView`).

## Context

Ebean already supports:

- Partial/flat DTO queries via `DB.findDto(...)` and `query.select(...).asDto(Dto.class)`.
- `@Formula` / `@Formula2` — path-based, auto-joined computed SQL expressions, but only on managed entities.
- `query.setUnmodifiable(true)` — builds a read-only, non-lazy-loading entity graph (`InterceptReadOnly`,
  see PR #2626). Accessing an unloaded property throws `LazyInitializationException`; mutating throws
  `UnmodifiableEntityException`.

Unlike Hibernate, Ebean does dirty-detection on the bean itself (no dynamic proxies), so there is very little
extra cost to an entity-graph query versus a DTO query. This makes an **unmodifiable entity graph** a cheap,
natural intermediate representation to map *from* when producing a DTO graph — we don't need Blaze/Hibernate's
proxy-based `EntityView` mechanism to get the performance benefit they are chasing.

The goal is nested DTO graph support (DTOs containing ToOne/ToMany child DTOs), not just today's flat DTOs,
while keeping DTOs as plain, framework-unattached classes.

## Accepted Requirements

### A. Nested DTO graphs

- **Support nested DTO graphs (ToOne/ToMany)**
  Allow mapping a query result into a DTO graph where DTO fields are themselves DTOs (ToOne) or
  `List`/`Set<Dto>` (ToMany), not just flat DTOs. Use the existing `setUnmodifiable(true)` entity graph as
  the intermediate, de-duplicated, identity-consistent source to map from.
  *Inspiration: Blaze `@EntityView` subviews/subview collections; Jimmer fetcher DTOs.*

- **Auto-generated entity → DTO graph mapper**
  Given an unmodifiable entity graph plus a target nested DTO type, generate (via annotation processing,
  reflection-free) a mapper that walks the graph and populates the DTO graph, matching properties by
  name/type with override annotations for renames, computed values, and collection element types.
  *Inspiration: Blaze `@EntityView` + subview mapping; conceptually similar to MapStruct but Ebean-generated
  and graph/identity aware.*

- **Identity-aware de-duplication in nested collections**
  When mapping nested collections referencing the same underlying entity instance multiple times, reuse the
  same DTO instance (mirrors Blaze/Jimmer identity semantics) rather than producing independent copies.
  *Inspiration: Blaze/Jimmer identity handling.*

### B. Formula-style DTO annotations

- **`@Formula2`-like annotations on DTO fields**
  Bring the existing `@Formula` / `@Formula2` concept (auto-joined, path-based computed SQL expressions) to
  DTO classes so a DTO field can request a computed/aggregated value with the join auto-derived, instead of
  only being available on managed entities.
  *Inspiration: User suggestion; Ebean `@Formula2`; Blaze `@Mapping` computed expressions.*
  *Status: a narrower version (pulling in an existing entity-level `@Formula2` by path) was implemented and
  then rejected - for the common same-name case it generated code identical to a plain unannotated field, so
  the annotation added no real value beyond a codegen-time validation. See `docs/dto-mapping-design.md`
  ("Formula2-on-DTO scope" and "Ad-hoc computed/formula properties" sections). The broader goal - arbitrary
  ad-hoc computed SQL on a DTO field - is better served by modelling the computed value as its own
  `@Entity @View`/`@Sql` read entity and mapping *that* into a plain DTO, reusing the existing (already
  accepted) nested-DTO mapping machinery rather than a new DTO-level annotation.

- **Path-based property mapping annotation on DTO**
  Allow a DTO field or constructor param to be annotated with a source path expression (e.g. `parent.name`)
  so Ebean can auto-derive the select clause plus joins for nested/renamed properties, reducing manual
  constructor wiring for non-trivial mappings.
  *Inspiration: Blaze `@Mapping`; QueryDSL constructor expressions.*

### C. Compile-time safety

- **Compile-time verification of `select(...).asDto(...)` mapping** *(explored, rejected as impractical -
  `mapTo()` accepted as the alternative)*
  Today `select(props).asDto(Dto.class)` is only checked at runtime. Explored an annotation-processor
  based mechanism to verify at compile time that selected properties match the DTO constructor or setters,
  mirroring QueryDSL's `@QueryProjection` compile-time Q-type generation. Rejected as impractical: raw SQL
  is an opaque `String` at compile time, and even the typed query-bean `.select(...)` form only
  type-checks against the *entity* - the match to the target DTO still happens at runtime via reflection
  (`DtoQueryPlanConstructor`), and the `.asDto(...)` call site can be arbitrarily distant from the
  `.select(...)` call, so there's no fixed AST shape an annotation processor could reliably verify.
  QueryDSL's actual compile-time safety comes from a different mechanism entirely - typed
  `Projections.constructor(...)`/generated Q-type constructor calls, not from checking an
  independently-built select-list against a DTO. `mapTo(Dto.class)` (see section A) already closes the
  underlying gap in the opposite, tractable direction: it derives the select/fetch spec *from* the DTO's
  declared shape at APT time, so it is compile-time safe by construction, with no separate select-list to
  drift out of sync. Recommendation: document `mapTo()` as the compile-time-safe answer for DTO
  projections, and treat `asDto()`/`findDto()` explicitly as the flexible, runtime-checked escape hatch
  for raw/dynamic SQL.
  *Inspiration: QueryDSL `@QueryProjection` compile-time Q-type generation.*

- **Fail-fast on unmapped or lazy property access**
  Ensure a clear, documented, minimal-ceremony way to fail fast if code touches a property not included in
  the query projection, instead of silently lazy loading or returning null. `query.setUnmodifiable(true)`
  already satisfies this (throws `LazyInitializationException`) — document/promote it as the answer, and
  evaluate whether a lighter-weight flag decoupled from full unmodifiable/read-only semantics is needed.
  *Inspiration: Original issue ask; already solved via `setUnmodifiable()` (PR #2626 `InterceptReadOnly`).*

### D. Fetch strategy and performance

- **Fetch strategy control for DTO graph relationships**
  Existing entity query fetch hints (join vs. select/subselect secondary query, `+query`/`+lazy`) should
  transparently carry over when the target of the query is a DTO graph rather than an entity graph.
  `query.mapTo(Dto.class)` applies the DTO-derived `FetchGroup` only when the query has no
  `select()`/`fetch()` already set - a manually tuned fetch spec always takes precedence and is never
  overridden, allowing manual query optimisation when needed (at the cost of falling back to the
  existing fail-fast-on-unmapped-property behaviour if the manual spec doesn't cover what the DTO needs).
  *Inspiration: Blaze FETCH/SELECT/SUBSELECT fetch strategies.*

- **Pagination support for DTO graph queries**
  Confirm existing pagination works unchanged when projecting into nested DTO graphs.
  *Inspiration: Blaze pagination and keyset pagination support.*

### E. Custom property conversion

- **Per-property custom scalar conversion (`@DtoConvert`)**
  Motivated by real hand-written mapper code (`DriverMapper`, central-access) doing per-property scalar
  coercion (`short` -> `boolean`) and dependency-backed conversion (AES decryption via an injected cipher).
  Introduce a `@DtoConvert(value = ConverterType.class, method = "name")` annotation (combinable with
  `@DtoPath` for source-getter override) on a DTO property. The generator dispatches based on whether the
  referenced method is `static`:
  - **Static method** -> a direct static call is inlined (`ConverterType.method(source.getX())`), zero
    ceremony, no registration - covers common, reusable, dependency-free scalar coercions (e.g.
    `short`/`boolean`, enum <-> `String`) that could apply across many unrelated entity/DTO pairs.
  - **Instance method** -> dispatched via `DtoConverterManager.get(ConverterType.class).method(source.getX())`
    and wired as a real constructor parameter/field on the generated mapper (same shape as existing
    nested-mapper constructor injection) - covers conversions needing a real dependency (e.g. a cipher).
  `DtoConverterManager` is a small, deliberately-scoped static put/get bridge: the app registers an
  already-DI-constructed converter singleton (e.g. built by avaje-inject) *before* building the `Database`.
  This is a narrow, accepted exception to the general no-static-mutable-state convention - it exists solely
  to bridge an already-DI-constructed singleton into `ServiceLoader`-discovered, no-arg-constructed generated
  code, which cannot otherwise reach a DI container. `DtoConverterManager.get(type)` throws immediately if
  nothing was registered, so a missing converter fails fast at Database-startup time (an eager field
  initializer on the generated `EbeanDtoMapperRegister`), not lazily on first use.
  *Design exploration considered and rejected two alternatives first: (a) a `DtoMapContext.service(Class)`
  lookup - rejected because `DtoMapContext` is a short-lived per-call identity-cache only, wrong lifetime for
  a real singleton dependency; (b) a `ServiceLoader`-discovered `DtoConverterSource` SPI mirroring
  `DtoMapperRegister` itself - rejected because a `ServiceLoader`-instantiated (no-arg) source cannot bridge
  to an *already* DI-constructed dependency (e.g. a cipher needing config/secrets) without reconstructing it
  itself, duplicating/bypassing the app's own DI-managed instance.*
  *Inspiration: `DriverMapper` (central-access) hand-written pattern; MapStruct qualified converter methods.*
  *Status: implemented - `@DtoConvert` (ebean-annotation), `io.ebean.DtoConverterManager` (ebean-api), and
  querybean-generator codegen support (static/instance dispatch, constructor wiring deduplicated by converter
  type). Test coverage: `tests/test-dto-mapping` `TestDtoConvert`.*

- **Type-pair (package-level) custom scalar conversion**
  Motivated by real hand-written mapper code (`EboxMapper`, central-access): the same conversion repeats
  across many unrelated properties on one target - `DateUtils.toCalendar(...)` on ~9 fields,
  `parseEnum(EnumType.class, value)` on ~3 - under today's `@DtoConvert` every one of those properties must
  carry its own repeated annotation. MapStruct solves this by letting a conversion method be defined once
  (in the mapper or a `uses = {...}` helper) and auto-applying it to *every* property whose source/target
  types match that method's signature - no per-field wiring. Proposed: a package-level, repeatable
  `@DtoConverters({ConverterType.class, ...})` (sibling to `@DtoMapping` in `package-info.java`) - the
  generator indexes every public static/instance method on the referenced type(s) by `(paramType ->
  returnType)`, then for any property whose source getter type doesn't already match the target field type
  and which carries no explicit per-property `@DtoConvert`, looks up that type pair and wires it in
  automatically (same static-vs-instance/`DtoConverterManager` dispatch rules as `@DtoConvert` today). An
  explicit per-property `@DtoConvert` always overrides the type-level default. Deliberately no built-in
  conversions shipped by Ebean itself (no implicit `Enum.valueOf`/`.name()`) - the app still owns
  exception/null-handling semantics (e.g. `parseEnum`'s catch-and-null-on-bad-value), just declares it once
  instead of per-field.
  **Status: implemented.** `@DtoConverters(ConverterType.class, ...)` (a single non-repeatable annotation
  taking a `Class<?>[]`, `@Target({PACKAGE, MODULE})`) is registered once per package/module alongside
  `@DtoMapping`. The generator indexes every public, single-arg, non-void method on each referenced type by
  exact `(paramType -> returnType)`; any SCALAR property (plain or `@DtoPath`-renamed) with no explicit
  `@DtoConvert` and a source/target type mismatch is auto-wired to the matching method (a duplicate/ambiguous
  type pair across the registered types is a compile-time processor error). List-element-wise conversion and
  `@DtoRef` (FK-id) properties are out of scope. Test coverage:
  `tests/test-dto-mapping/.../TestDtoConverters.java` (`UuidConverters`/`UuidShortCodeConverter`,
  `ContactTypeConverterDto`) - covers same-name auto-dispatch, `@DtoPath`-renamed auto-dispatch, and explicit
  `@DtoConvert` overriding the registered default.
  *Inspiration: `EboxMapper` (central-access) hand-written pattern; MapStruct type-signature-matched
  conversion methods.*

- **`@DtoMixin` for DTOs that cannot be annotated directly**
  Some DTOs are generated (e.g. from an OpenAPI spec) and not editable/annotatable, so `@DtoPath`/
  `@DtoConvert`/`@DtoRef` cannot always be placed directly on the DTO. Introduce a `@DtoMixin(Target.class)`
  companion interface/type, discovered by scanning the compilation round and overlaying its per-property
  annotations onto the real target's properties by name-match - directly mirrors avaje-jsonb's
  `@Json.MixIn` mechanism (`KingfisherMixin`/`CrewMateMixIn` pattern), a proven prior-art solution to the
  exact same "can't annotate a generated/unowned type" problem.
  *Inspiration: avaje-jsonb `@Json.MixIn`.*
  *Status: implemented - `@DtoMixin` (ebean-annotation) and querybean-generator round-scanning/overlay
  support (matches mixin methods to target properties by name, applying whichever of `@DtoPath`/`@DtoRef`/
  `@DtoConvert` is present as if declared on the target field itself). Test coverage: `tests/test-dto-mapping`
  `TestDtoMixin`.*

### F. DI-friendly manual mapper usage

- **Public `DtoMapperManager` with `get(Class<T> mapperType)` for DI**
  Motivated by `DriverMapper`/`DriverService` (central-access): `DriverMapper` is a hand-written
  `@Component` constructor-injected into `DriverService`. Moved `DtoMapperManager` from internal
  (`io.ebeaninternal.server.dto`) to public `io.ebean` - unchanged `mapperFor(source, dto)`, plus a new
  `get(Class<T> mapperType)` keyed by the generated mapper's own concrete class (e.g.
  `manager.get(CustomerDtoMapper.class)`), for direct/concrete-typed DI injection. `DtoMapperRegister`
  gained a default `mapperOfType(Class<T>)` method (non-breaking); the generator emits the real if-chain
  body (mirrors `mapperFor`'s if-chain). `DtoMapperManager` has zero `Database` dependency (constructor
  only does `ServiceLoader.load(DtoMapperRegister.class)`), so it can be constructed standalone,
  independent of/before a `Database` - e.g. as an avaje-inject bean.
  *Inspiration: `DriverMapper`/`DriverService` (central-access).*
  *Status: implemented.*

- **`DtoMapperManager` sharing via `DatabaseBuilder.putServiceObject`**
  So `query.mapTo()` and application-injected mappers share the exact same `DtoMapperManager` instance
  (and hence the same underlying generated mapper singletons) rather than each independently constructing
  its own, `InternalConfiguration` checks `config.getServiceObject(DtoMapperManager.class)` first (mirrors
  the existing `AutoMigrationRunner`/`GeoTypeProvider` `putServiceObject`/`getServiceObject` pattern),
  falling back to constructing a default `new DtoMapperManager()` if none was supplied.
  *Inspiration: user proposal following `DriverMapper`/`DriverService` review.*
  *Status: implemented.*

*Rejected: generator-emitted `builder(source)` method* - `DriverMapper` exposes `builder(cDriver)`
returning a partially-populated `DriverBuilder` so callers can add extra caller-supplied fields (e.g.
fleets) before `build()`. Rejected as a generator feature - `Driver`/`DriverSummary` already use
avaje-recordbuilder's `@RecordBuilder`, which generates `Target.builder(existingInstance)`
(seed-from-instance). The same effect is already achievable with zero ebean changes:
`mapper.map(source)` then `Builder.builder(mapped).extraField(x).build()`. Documented as a recipe
instead (see "Recipe: adding extra caller-supplied fields after mapping" in
`docs/guides/mapping-entity-graphs-to-dtos.md`).

### G. Large-target construction and shape variants

- **Builder-based target construction for large DTOs**
  Motivated by `UserService`/`User` (central-access): `User` is a 24-field OpenAPI-generated record with
  a `@RecordBuilder`-generated `UserBuilder`, hand-mapped via a long fluent builder chain rather than a
  positional constructor to stay readable/refactor-safe. The generator auto-detects a RecordBuilder-style
  builder on the target (static `Target.builder()` + fluent per-property setters + `build()`) and uses
  `Target.builder().prop(x)....build()` instead of `new Target(a, b, c, ...)` whenever (a) a builder is
  detected and (b) the target has more than a threshold number of properties (default 5), falling back to
  the positional constructor otherwise. An explicit `@DtoMapping` attribute (`builder = AUTO | ALWAYS |
  NEVER`) overrides the heuristic in either direction. Applies regardless of whether the target class is
  hand-authored or foreign/generated (e.g. an OpenAPI record) - `@DtoMapping` is already declared
  externally via `package-info.java`, not on the target class, so this was already compatible with
  foreign target types.
  *Inspiration: `UserService`/`User` (central-access).*
  *Status: implemented.*

- **Named mapper variants excluding nested paths, sharing one generated class**
  Motivated by `UserService`/`User` (central-access): `CUser` -> `User` is mapped in two shapes - with
  nested `fleets` (`findUserByGid`) and without (`findAll`, bulk listing) - to avoid an unnecessary
  join/fetch on the common bulk-listing path. Keeps the existing "shape always derived from declaration,
  fetch spec always wins" philosophy (rejected relaxing that rule / rejected a runtime
  is-property-loaded auto-skip check as less deterministic). The same `(source, target)` pair can be
  declared more than once in `package-info.java` via a named variant, e.g.
  `@DtoMapping(source = CUser.class, target = User.class)` (base/full) plus
  `@DtoMapping(source = CUser.class, target = User.class, name = "noFleets", exclude = "fleets")`
  (variant). Both variants are generated into the **same** mapper class (one class per target, not one
  per variant) and share a single private `build(source, context, boolean includeXxx, ...)` method
  containing the common field population written once; each excluded nested path becomes a `boolean
  includeXxx` parameter of that shared method rather than a precomputed value, so `build()` still
  evaluates every property - included or excluded - inline, at its own declared field position (a
  `includeFleets ? fleetsMapper.mapList(...) : List.of()` ternary in place, not hoisted out as a
  pre-evaluated call argument). This preserves the DTO's declared property order as the true evaluation
  order regardless of which properties a variant happens to exclude. The base `map()` passes `true` for
  every flag; each named variant (exposed as a same-named accessor, e.g. `noFleets()`, returning a single
  shared/cached instance of its own small `DtoMapper<SOURCE, TARGET>`-implementing inner class - not
  reconstructed per call) passes `false` for the paths it excludes and omits that path from its own
  `fetchGroup`. Selected via a new `query.mapTo(Class<D> dtoType, DtoMapper<T, D> mapper)` overload
  taking an already-resolved mapper instance directly (e.g. `query.mapTo(User.class,
  userMapper.noFleets())`) - no string-based variant lookup, and no changes needed to
  `DtoMapperRegister`/`DtoMapperManager`.
  *Inspiration: `UserService`/`User` (central-access).*
  *Status: implemented.*

- **Setter-based (mutable JavaBean) target construction**
  Motivated by `EboxMapper` (central-access): its target types (`Ebox`, `MachineSummaryInfo`, from
  `nz.co.eroad.schema.eroadtypes`, JAXB/XSD-generated legacy SOAP shapes) are plain mutable JavaBeans - a
  public no-arg constructor plus a `void setXxx(...)` setter per property - neither a positional constructor
  match nor a RecordBuilder-style fluent builder (see section G above). The generator currently only
  recognizes those two construction strategies, so this common third shape (typical of JAXB/XSD-generated
  and many hand-written mutable POJOs) can't be targeted by `@DtoMapping` at all today. Proposed: detect a
  no-arg constructor plus a `void setXxx(propertyType)` setter per mapped property as a third construction
  strategy, generating `Target target = new Target(); target.setX(...); ...; return target;` (mirroring the
  existing `build = AUTO | ALWAYS | NEVER` override precedent from section G for explicit control over which
  strategy applies). Would also unblock the `mapToBuilder()`-style "populate ignored/derived properties after
  the generated mapping, before finishing construction" pattern for these targets (currently only available
  for builder-shaped targets) - relevant to `EboxMapper`'s `machineSummaryInfo` (a genuinely composite,
  multi-association derived value, out of reach of `@DtoConvert`/`@DtoPath` regardless of this gap, but a
  natural fit for the same "map base fields via codegen, then set the derived one by hand" pattern already
  used for `Fleet.assignedMachines`/`assignedDrivers`).
  *Inspiration: `EboxMapper` (central-access); JAXB/XSD-generated SOAP DTO shapes generally.*
  **Status: implemented.** `@DtoMapping(setter = AUTO | ALWAYS | NEVER)` mirrors `builder()`'s override
  precedent. Detection requires a public no-arg constructor plus a public `setXxx(...)` setter for every
  mapped property - either `void` or fluent-style (returning the target type itself, e.g. `public Target
  setXxx(...) { ...; return this; }`); the generated code always calls the setter as a bare statement and
  discards any return value, so either shape works identically. A builder, when selected, always takes
  priority over setter-based construction. Under the default `AUTO`, setter-based construction is only
  attempted when the target has no positional constructor matching the mapped properties (arity-based) and
  no builder was selected - existing positional-constructor and builder-shaped targets are entirely
  unaffected. `ALWAYS` requires the shape (codegen-time error otherwise); `NEVER` always uses a positional
  constructor. Generated shape: `Target target = new Target(); target.setX(...); ...; return target;` (a
  `computeIfAbsent(...)`-wrapped block-lambda variant when the target is nested elsewhere in the graph).
  Deliberately **no** `mapToBuilder(...)`-style post-construction accessor is generated for this strategy -
  the returned target is already the final, fully mutable instance (setters are required to be `public`), so
  a caller can already call e.g. `dto.setExternalRef(...)` directly on the mapped result, exactly the pattern
  `EboxMapper` already uses by hand; this is unlike the builder strategy, where the intermediate builder is
  otherwise unreachable after its one-shot `build()` call. Test coverage:
  `tests/test-dto-mapping/.../TestDtoSetterConstruction.java` (`ContactSetterDto`) - covers auto-detected
  setter-chain construction plus post-construction population of two `@DtoIgnore` properties (a plain scalar
  and a `List`) via their public setters; plus `ContactSetterFluentDto` - covers the fluent-setter-return-shape
  variant.

### H. Record entity sources

- **Record-style (bare/fluent) accessors on the source (entity) side**
  Ebean supports entity beans declared as Java `record`s (e.g. `public record CourseRecordEntity(@Id long id,
  String name, String notes) {}` - see `test-java16`), whose only accessor shape is the bare component name
  (`active()`, `name()`, `id()`) - never `getXxx()`/`isXxx()`. This bare-accessor convention isn't limited to
  an actual `record` type though - an ordinary class can just as easily expose bare/fluent-style accessors
  with no `get`/`is` prefix at all. The generator resolves the real accessor for each source type (the direct
  source, or an intermediate `@DtoPath`/`@DtoRef` association type) by checking which shape actually exists as
  a method, in order: (1) `isXxx()` returning `boolean` (JavaBean boolean convention), (2) `getXxx()` (JavaBean
  convention), (3) the bare `propertyName()` itself - falling back to a guessed `getXxx()` only if none of the
  three are found. Resolution is entirely name/existence-based (no dependency on whether the type is actually
  a `record`). The Ebean bean-property name used in generated `FetchGroup.select(...)`/`.fetch(...)` calls is
  tracked directly from the original property/segment name (not reverse-parsed from the resolved accessor's
  method name), so it's correct regardless of which of the three accessor shapes was used.
  *Inspiration: Ebean's own record-entity support (`test-java16`); user-reported gap during review.*
  *Status: implemented.*

## Rejected Requirements

These were considered and explicitly rejected as out of scope:

- **DTO as interface / dynamic proxy views** — Blaze `@EntityView` defines views as interfaces backed by
  runtime proxies. This conflicts with Ebean's preference for plain, framework-unattached DTO classes.
- **Updatable or creatable entity views (persist through DTO)** — Blaze's `@UpdatableEntityView` /
  `@CreatableEntityView` cascade persist/update through the view. This would duplicate Ebean's existing
  entity persistence model and introduce a second, ambiguous dirty-checking/cascade model.
- **New predicate/filter DSL for subview collections** — Blaze allows filter expressions directly in
  `@Mapping` (e.g. filtering a collection by an attribute value). Ebean already has typed query bean
  predicates and `.filterMany()` for filtering child collections in queries; no new embedded filter
  expression language is needed on the DTO itself.

## References

- Issue: https://github.com/ebean-orm/ebean/issues/2540
- PR #2626: `InterceptReadOnly` / `InterceptReadWrite` split enabling the unmodifiable entity graph fast path
- Ebean docs: https://ebean.io/docs/query/option#unmodifiable
- QueryDSL: `@QueryProjection` (constructor-based, compile-time-checked projections)
- Blaze-Persistence Entity Views: https://persistence.blazebit.com/documentation/1.6/entity-view/manual/en_US/
