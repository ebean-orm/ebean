# Nested DTO Mapping — API Design

Design spike for the accepted requirements in [dto-mapping-requirements.md](./dto-mapping-requirements.md),
covering issue #2540. This captures the concrete API shape, annotations, and open-question decisions made
during design review — before implementation begins.

## Two source-vs-target mapping pipelines

Ebean now has (or will have) two distinct DTO pipelines. It's important callers can tell which one they're
using:

1. **`asDto(Dto.class)`** (existing) — a `DtoQuery`, executed directly against a flat SQL `ResultSet`.
   One row -> one DTO, via constructor/setter matching. No nested ToOne/ToMany support, no entity graph
   involved.
2. **`mapTo(Dto.class)`** (new) — runs the normal ORM entity query (joins/fetches as usual), producing an
   *unmodifiable entity graph*, then maps that Java object graph into a DTO graph. Supports nested
   ToOne/ToMany, identity-aware de-duplication, and derives its own fetch spec from the DTO shape.

## Proposed API

```java
// Existing flat DtoQuery pipeline — unchanged
new QUser().valid.eq(true)
    .select(firstName, lastName)
    .asDto(UserInfo.class)
    .findList();
```

```java
// NEW: nested DTO graph pipeline
public class CustomerDto {
  Long id;
  String name;
  AddressDto billingAddress;      // ToOne -> nested DTO, matched by property name "billingAddress"
  List<ContactDto> contacts;      // ToMany -> nested DTO list, matched by property name "contacts"

  @DtoPath("billingAddress.line1")
  String billingLine1;            // renamed / flattened path
}

public class ContactDto {
  Long id;
  String firstName;
  String lastName;

  @DtoRef
  Long customerId;                // id-only back-reference, avoids re-embedding CustomerDto (cycle)
}

List<CustomerDto> dtos = new QCustomer()
    .status.eq(Status.ACTIVE)
    .mapTo(CustomerDto.class)
    .findList();
```

Computed values (e.g. `cityOrUnknown` derived from `coalesce(billingAddress.city, 'Unknown')`) are
**not** modeled with a `@Formula2` annotation directly on the DTO - that was explored and rejected
(see "Formula2-on-DTO scope" below). Instead they're modeled as a plain matching field on the DTO,
sourced from an `@Entity @View` entity that itself carries the `@Formula2` - see "Computed/aggregate
properties" below for the worked example.

`mapTo(CustomerDto.class)`:
- Introspects `CustomerDto` (recursively, at codegen time) to derive the `select(...)`/`.fetch(...)` spec
  automatically from the DTO's declared shape.
- Forces `setUnmodifiable(true)` under the hood — gives fail-fast + a cheap, non-mutable source graph
  (satisfies the fail-fast requirement without a separate flag).
- Runs the query, then runs a mapper over the resulting entity graph, de-duplicating DTO instances by id
  for repeated nested references (identity-aware, mirrors the source entity graph's own de-duplication).

## Decisions made

### Fetch spec: auto-derived from DTO shape

The DTO's declared structure (fields, nested DTO types, `@DtoPath` overrides) is the single source of truth
for what gets selected/fetched from the database. Callers do not need to separately maintain a `.fetch(...)`
spec in parallel with the DTO — this directly addresses the original issue's pain point (DTO and query
projection drifting out of sync).

### Entry point naming: `mapTo(Dto.class)`

Chosen over `asGraph(...)` / `into(...)` / overloading `findList(Class)`. Reads clearly as "map the
resulting entity graph to this DTO type" and is unambiguous against the existing `asDto(...)` (flat,
SQL-row-based) mechanism.

### Cycle handling: codegen-time DAG check + `@DtoRef` escape hatch

Because the fetch spec and mapper are both derived from the *static* DTO type graph (not live object
traversal), cycle detection is a compile-time/codegen-time concern, not a runtime one. This is stronger
than the common approach in the ecosystem:

- **MapStruct** does not auto-detect cycles. It offers an opt-in `@Context` "cycle guard" pattern (an
  identity map of already-mapped source -> target objects) that the developer must wire up manually to
  avoid infinite recursion mapping bidirectional object graphs.
- **Blaze-Persistence / QueryDSL / JOOQ record mapping** avoid the problem architecturally: view/projection
  types are required to be a strict tree; a back-reference is modeled as an id or a much shallower type,
  never the same full view type again.

Ebean's approach: fail the build at annotation-processing time if a DTO's declared type graph is not a DAG,
with a clear error message. Provide `@DtoRef` as an explicit escape hatch for intentional back-references
(e.g. `Contact.customer`) — it maps only the id, not the full nested DTO, breaking the cycle by design
rather than by runtime guard.

### `@DtoPath` / `@DtoRef`: parallels for readers coming from MapStruct or Blaze-Persistence

Neither annotation is a novel concept - both map onto things MapStruct and Blaze-Persistence users will
already recognise, which is worth spelling out explicitly so it's easy to "grok fast":

- **`@DtoPath("billingAddress.line1")` is Ebean's equivalent of MapStruct's dot-path `source` flattening**
  — e.g. `@Mapping(target = "line1", source = "billingAddress.line1")`. MapStruct auto-generates a
  null-safe chain of getter calls for a dotted `source`; `@DtoPath` does exactly the same thing, just
  declared on the DTO field itself rather than on a mapper method parameter list. It is also close to
  Blaze-Persistence's `@Mapping("billingAddress.line1")` on an `@EntityView` attribute, which is a JPQL
  path expression evaluated the same way — Blaze's placement (directly on the target view property) is
  actually the closer analogue of the two, since Ebean's `@DtoPath` is likewise placed on the DTO field.
  The difference from Blaze: `@DtoPath` is restricted to plain getter-chain navigation (no arbitrary JPQL/
  SQL expression) - see "Formula2-on-DTO scope" below for the boundary and why full expression support is
  deliberately deferred.
- **`@DtoRef` has no dedicated equivalent in either tool** - both MapStruct and Blaze would express the
  same "just the id" mapping as a plain dot-path to `.id` (`@Mapping(source = "customer.id")` / Blaze
  `@Mapping("customer.id")`), with no special marker for it. What `@DtoRef` adds beyond that shorthand is
  *intent*: it tells the codegen this property is a deliberate cycle-breaking reference, so (a) it adds
  just the association's own name (not a dotted `.id` path) to the generated fetch spec's root
  `select(...)` - reading the FK column directly with no join, and skipped entirely if that same
  association is already fully fetched by a `NESTED_ONE`/`NESTED_MANY` property elsewhere on the same DTO
  (see `DtoMapperWriter.fetchGroupChainCalls()`'s `case REF` branch) - and (b) it participates in the
  codegen-time DAG cycle check above as an explicit "this is fine, don't flag it" signal, rather than
  requiring a suppression escape hatch bolted on afterwards.

  **Bug found and fixed while building the aggregation worked example below:** the original implementation
  excluded `REF` properties from the fetch spec *entirely*, on the assumption the id is "already available
  off an unfetched reference without triggering a fetch/lazy load". That assumption is only true when some
  *other* property on the same DTO happens to also fetch that association (as was always the case in the
  existing hand-built examples). Tested directly against a bare `@ManyToOne` with no other fetch of it:
  accessing `.getCustomer().getId()` in that case triggers a full lazy-reload of the owning row (extra SQL,
  not free) - and for an aggregation query it's worse, since the property being grouped by must be selected
  or the query can't group correctly at all. Fixed so `REF` always contributes its association name to the
  root `select(...)` (deduped against any existing `NESTED_ONE`/`NESTED_MANY` fetch of the same path).

**Bug found and fixed (validation phase, testing against `central-access`): primitive-typed field +
nullable intermediate hop = unboxing `NullPointerException`.** A multi-hop `@DtoPath` (or `@DtoRef`,
which is always 2-hop) null-guards each intermediate getter with a ternary, e.g.
`(source.getOrganisation() == null ? null : source.getOrganisation().getId())`. That ternary's static
type is always the boxed wrapper (`Long`), since one branch is the `null` literal - fine when the DTO
field is itself a reference type (`Long organisationId`), but when the DTO field is a **primitive**
(`long organisationId`), passing that boxed expression to the constructor auto-unboxes it, throwing an
unhelpful `NullPointerException` at runtime whenever the relation really is `null`. This compiled clean
and only failed at runtime with real (nullable) production data - exactly the kind of gap a hand-written
mapper would defensively guard against (e.g. `cEbox.getOrganisation() == null ? 0 : ...getId()`) but
generated code didn't.

Fixed in the generator: when a multi-hop `SCALAR`/`REF` property's DTO field type is primitive, the
whole null-guarded chain is now wrapped in a small runtime helper (`io.ebean.DtoMapperSupport`) that
resolves it safely:
- **Default** (`@DtoPath` with no `failOnNull`, or any `@DtoRef`): silently defaults to the primitive's
  zero-equivalent value (`0`/`false`/etc.) - matches the old hand-written-mapper convention.
- **`@DtoPath(failOnNull = true)`**: throws a clear `IllegalStateException` naming the offending property
  path instead, for callers who'd rather fail fast than silently mask a null they don't expect.

`@DtoRef` has no `failOnNull` attribute (it has no other attributes at all) - it always uses the
default (silent zero) behaviour. See `PrimitiveNullPathDto`/`PrimitiveNullPathFailOnNullDto` /
`TestPrimitiveNullPath` for regression coverage.

### Read-only entity memory overhead: `InterceptReadOnly`
`setUnmodifiable(true)` isn't just a behavioural fail-fast flag - it also swaps the per-bean intercept
implementation to `InterceptReadOnly`, which is deliberately minimal: just a `boolean[] loaded` (one flag
per property) and a `boolean frozen`, plus the inherited owner reference and `fullyLoadedBean` flag. Compare
to `InterceptReadWrite` (the mutable/updatable variant), which additionally carries a `ReentrantLock`, four
transient collaborator references (`NodeUsageCollector`, `PersistenceContext`, `BeanLoader`,
`PreGetterCallback`), a `byte[] flags` array (per-property loaded+changed+dirty+orig-value-set state),
`Object[] origValues`, `Exception[] loadErrors`, `MutableValueInfo[]`/`MutableValueNext[]`, and several more
scalar bookkeeping fields. None of that is needed for a bean that will only ever be read, so
`setUnmodifiable(true)` graphs carry meaningfully less per-instance overhead than normal fetched entities -
relevant here because `mapTo(Dto.class)` forces `setUnmodifiable(true)` on its underlying query, making the
*source* graph for a DTO mapping cheaper than the equivalent normal (writable) entity graph would be.

### Ad-hoc computed/formula properties: model as `@Entity @View`/`@Sql`, not ad-hoc SQL-on-DTO

The "fully ad-hoc SQL-on-DTO" stretch goal above (closer to Blaze's arbitrary `@Mapping` expressions) doesn't
need to be built as a bespoke DTO-annotation-processing feature. Ebean already supports modelling read-only,
computed, or view-backed data as ordinary entities via `@Entity` + `@View` (backed by a SQL view, e.g. one
with aggregates/computed columns) or `@Entity` + `@Sql` (backed by arbitrary `RawSql`, no base table). Given
that, the Blaze-Persistence-style "an entity view attribute backed by an arbitrary SQL expression" need can
usually be satisfied by:

1. Modelling the computed/derived shape as its own `@Entity @View` (or `@Sql`) "read entity" - the SQL
   expression/aggregation lives in the view definition, not in a new annotation-processed DTO mechanism.
   `@View`'s `name()` doesn't have to point at a genuinely separate database view - it can just point at
   an *existing* table (e.g. `@View(name = "contact")` on a second entity class reading the same table as
   `Contact`) purely to mark the entity as view-like/read-only, in which case Ebean's DDL generator emits
   **no new table or view at all** for it - it's just a second lens onto the same physical data.
2. Mapping *that* entity into a plain DTO using the existing, already-implemented `@DtoMapping` machinery -
   no ad-hoc-SQL-on-DTO support required, since there's no computed expression left to resolve at the DTO
   layer at all; it's just another entity-to-DTO mapping.
3. This read entity benefits from the same `setUnmodifiable(true)`/`InterceptReadOnly` memory efficiency
   above when used purely as `mapTo(...)` input, so there's no meaningful cost to preferring this over a
   hypothetical native ad-hoc-SQL-on-DTO feature.

This significantly narrows (and may eliminate) the case for a dedicated ad-hoc-SQL-on-DTO mechanism - it
remains listed as an open stretch goal below primarily for the case where a computed value's SQL is genuinely
one-off/DTO-specific and not worth promoting to a standalone `@View`/`@Sql` entity.

**Worked example** (`tests/test-dto-mapping`): `ContactSummary` is `@Entity @View(name = "contact")` (no new
DDL - reads the same table as `Contact`) with `@Formula2("concat(firstName, ' ', lastName)")` computing
`fullName`; `ContactSummaryDto` is a plain two-field DTO; `@DtoMapping(source = ContactSummary.class, target
= ContactSummaryDto.class)` generates `ContactSummaryDtoMapper` exactly like any other entity→DTO pair - the
formula property is just selected like any other field (`select("id,fullName")` in the generated
`fetchGroup()`). See `TestContactSummaryDtoMapping`.

### Aggregate/group-by computed properties: `@Sum`/`@Aggregation` as `@Entity @View`, same pattern

Ebean's `@Sum` (shorthand for `@Aggregation("sum($1)")`) and `@Aggregation("count(...)"/"sum(...)"/"avg(...)"/
"min(...)"/"max(...)")` are the group-by parallel to the formula pattern above - the same `@Entity @View`
approach applies, just with an implicit `GROUP BY` instead of a per-row computed column. Ebean auto-derives
the `GROUP BY` clause from whichever non-aggregate properties end up in the query's `select()`/`fetch()` - so
a second `@Entity @View(name = <same base table>)` entity with one or more `@Sum`/`@Aggregation` properties
plus a `@ManyToOne` grouping key becomes a per-parent rollup, with **no new table/view and no explicit
`.groupBy()` call required**. This is Ebean's parallel to Blaze-Persistence entity view correlated aggregate
mappings, e.g. `@Mapping("SIZE(contacts)")` / `@Mapping("SUM(contacts.engagementScore)")` on an `@EntityView`.

**Nuance found while building the worked example, and since fixed: `@DtoRef` originally didn't fit the
grouping key.** `@DtoRef` was originally excluded from the generated `select()`/`fetch()` spec entirely, on
the premise that the id is already available off an unfetched reference for an ordinary entity graph. That
premise doesn't hold for an aggregation query: the `@ManyToOne` *is* the property being grouped by, so if
it's never selected, the query has nothing to group by. It turned out the premise didn't fully hold for
ordinary entity graphs either - see the `@DtoRef` bug writeup above. Fixed so `@DtoRef` now adds the
association's own name to the root `select(...)` (reading the FK column directly, no join) - which both
supplies the grouping key here and fixes the general-case gap.

**Worked example** (`tests/test-dto-mapping`): `ContactStats` is `@Entity @View(name = "contact")` (no new
DDL - reads the same table as `Contact`/`ContactSummary`) with `@Aggregation("count(id)") contactCount` and
`@Sum Integer engagementScore` (a new nullable field added to `Contact` purely to have something to sum),
grouped by its `@ManyToOne customer`. `ContactStatsDto` is a flat 3-field DTO (`customerId`, `contactCount`,
`engagementScore`), with `customerId` mapped via plain `@DtoRef`. The generated `ContactStatsDtoMapper`:

```java
this.fetchGroup = FetchGroup.of(ContactStats.class)
  .select("customer,contactCount,engagementScore")
  .build();
...
// skip DtoMapContext, only ever a top-level mapping
return new ContactStatsDto(
  (source.getCustomer() == null ? null : source.getCustomer().getId()),
  source.getContactCount(),
  source.getEngagementScore());
```

confirmed (via `LoggedSql`) to produce `select t0.customer_id, count(t0.id), sum(t0.engagement_score) from
contact t0 ... group by t0.customer_id` - **no join**, one row per customer, correctly summed and counted.
See `TestContactStatsDtoMapping`.

### Formula2-on-DTO scope (v1): existing entity formulas only

`@Formula2` on a DTO property in v1 only pulls in a formula **already declared on the source entity** (or
a reachable associated entity) — it does not support fully ad-hoc SQL declared directly on the DTO with no
matching entity property. Fully ad-hoc SQL-on-DTO (closer to Blaze's arbitrary `@Mapping` expressions) is a
separate, larger stretch goal to revisit once the core graph-mapping mechanism is proven.

**Attempted and rejected for v1.** A narrower version was implemented (`@Formula2(value)` resolved exactly
like `@DtoPath` - a dot-path getter chain - plus a codegen-time validation that the resolved entity property
is itself `@Formula2`/`@Formula`-annotated) but was rejected: for the common case (a DTO field with the same
name as the entity's formula property) it generated **identical code to a plain unannotated field** - the
only difference was the validation, which wasn't judged enough distinct value to justify a new annotation
surface. Not implemented. The only way `@Formula2`-on-DTO would add real value is the full ad-hoc-SQL
capability described above, which remains an open stretch goal.

### Mapper implementation strategy: codegen, not reflection (native-image constraint)

Native-image support is a core Ebean requirement, so the entity-graph -> DTO-graph mapper must not rely on
runtime reflection or `MethodHandles`. This ruled out an initial reflection-based spike:

- Ebean's existing flat `DtoQuery` (`DtoMetaConstructor`) already uses `MethodHandles` via
  `Lookups.getLookup()`, but there is no `reflect-config.json` / native-image reachability metadata shipped
  for it anywhere in the repo. That existing approach is not a clean precedent to copy for a bigger,
  native-image-first feature.
- Instead, the approach mirrors `querybean-generator`, which already generates real `.java` source for
  `Q*` query bean types (not reflection) — consistent with the wider avaje-ecosystem convention
  (avaje-inject / avaje-jsonb are explicitly reflection-free via compile-time codegen).

**Implementation sequencing:** hand-write the mapper in the exact shape the annotation processor will
eventually generate (plain Java, direct getter/constructor/setter calls, zero reflection) for one concrete
example first, to validate the mapping algorithm and API shape quickly without ever introducing throwaway
reflective code. That hand-written mapper then becomes the target/acceptance-test shape for the
`querybean-generator` annotation processor that automates producing it.

### Codegen target: Java first

The mapper generation (requirement r2) targets `querybean-generator` (the existing APT module that already
generates `Q*` query beans, reusing its `PropertyMeta` / `ProcessingContext` machinery). Kotlin parity via
`kotlin-querybean-generator` is deferred to a later phase — not blocking initial delivery.

### Mapper composition: one mapper per entity/DTO pair, generic `DtoMapper<SOURCE, TARGET>` interface

Rather than one large mapper inlining every nested DTO type, each entity/DTO pair gets its own small
mapper class - mirroring MapStruct's per-type mapper generation. All mappers implement a shared generic
interface (prototyped as `org.tests.dtomapping.DtoMapper<SOURCE, TARGET>` in the spike, expected to move to
`io.ebean` as a public type once solidified):

```java
public interface DtoMapper<SOURCE, TARGET> {
  TARGET map(SOURCE source);
  default List<TARGET> mapList(List<SOURCE> source) { ... }
}
```

A parent mapper composes nested mappers via **constructor injection**, not a static singleton:

```java
public final class CustomerDtoMapper implements DtoMapper<Customer, CustomerDto> {
  private final DtoMapper<Address, AddressDto> addressMapper;

  public CustomerDtoMapper() {
    this(new AddressDtoMapper());
  }

  public CustomerDtoMapper(DtoMapper<Address, AddressDto> addressMapper) {
    this.addressMapper = addressMapper;
  }

  @Override
  public CustomerDto map(Customer source) {
    if (source == null) return null;
    return new CustomerDto(source.getId(), source.getName(), addressMapper.map(source.getBillingAddress()));
  }
}
```

Rationale:
- **Composability & reuse** - the same nested DTO type (e.g. `AddressDto`) used from multiple parent DTOs
  reuses one generated mapper class rather than duplicating inline mapping logic.
- **Constructor injection over static state** - avoids a global mutable singleton; a no-arg constructor
  gives the common case (default nested mapper), while an overload accepting the nested mapper explicitly
  allows substitution (tests, customization) without touching global state.
- **Codegen-friendly** - this shape generates naturally: one top-level mapper class per DTO type, each
  constructor-injecting the mappers for any nested DTO types it references.

## ToMany collections and identity de-duplication (dto-spike-tomany-identity)

Extending the spike (`ebean-test/src/test/java/org/tests/dtomapping/`) to a `Customer` with a
`List<Contact> contacts` ToMany, where each `Contact` has a `customer` back-reference, surfaced
two things worth recording.

### The `DtoMapper` interface threads a shared context

`DtoMapper<SOURCE, TARGET>` was extended so that mapping is always done against a `DtoMapContext`:

```java
public interface DtoMapper<SOURCE, TARGET> {
  TARGET map(SOURCE source, DtoMapContext context);

  default TARGET map(SOURCE source) {
    return map(source, new DtoMapContext());
  }

  default List<TARGET> mapList(List<SOURCE> source, DtoMapContext context) { ... }

  default List<TARGET> mapList(List<SOURCE> source) {
    return mapList(source, new DtoMapContext());
  }
}
```

`DtoMapContext` is an identity-keyed cache of already-mapped source -> target instances, created
once per top-level `mapList(...)`/`map(...)` call and threaded through every nested `map(...)`
call. This lets repeated references to the *same* source entity instance - which Ebean's own
persistence context already de-duplicates within one query (`contact.getCustomer() == customer`
for the enclosing `Customer`, confirmed by an existing test) - map to the *same* target DTO
instance, rather than each producing an equal-but-distinct copy. This is what makes the mapped
DTO output "graph shaped" rather than "tree of copies shaped", and is required for r1/r3.

### Bug found and fixed: the cache must be partitioned by target type, not just source identity

The first cut of `DtoMapContext` was a single `IdentityHashMap<Object, Object>` keyed only by the
source instance. This breaks as soon as the *same* source instance legitimately needs to map to
*two different target types* within one graph - which happens immediately with a back-reference:

- The top-level `CustomerDtoMapper` maps a `Customer` -> full `CustomerDto`.
- The nested `ContactDtoMapper`, mapping `contact.getCustomer()` (the *same* `Customer` instance,
  by identity), maps it -> shallow `CustomerRefDto` (the `@DtoRef`-style escape hatch that avoids
  the `Customer -> Contact -> Customer` cycle).

With a single un-partitioned identity map, whichever mapper runs first "wins" the cache slot for
that `Customer` instance, and the other mapper incorrectly receives the wrong-typed cached result
(a `ClassCastException` at best, silently wrong data at worst). This was caught by a failing test
during the spike and fixed by partitioning the cache per target type:

```java
public final class DtoMapContext {
  private final Map<Class<?>, Map<Object, Object>> mappedByType = new HashMap<>();

  public <S, T> T computeIfAbsent(Class<T> targetType, S source, Function<S, T> mappingFunction) {
    Map<Object, Object> mapped = mappedByType.computeIfAbsent(targetType, t -> new IdentityHashMap<>());
    // ... existing/create/put ...
  }
}
```

Each generated mapper passes its own target DTO `Class` as the first argument, so `Customer ->
CustomerDto` and `Customer -> CustomerRefDto` are cached independently even though the key
(`Customer` instance) is identical. **This is an implementation detail the codegen must get
right** - worth flagging explicitly when `dto-codegen-mapper` starts, since it's easy to
regress if the generator is written from scratch without this test coverage in front of it.

### Codegen optimization: skip the `DtoMapContext` cache for types that are never nested elsewhere

`DtoMapContext.computeIfAbsent` only ever produces a cache *hit* when the exact same source
instance is presented to `map()` more than once within one top-level call - which can only happen
when the target type is reachable via more than one path in the graph, i.e. it's used as a
`NESTED_ONE`/`NESTED_MANY` property by some *other* `@DtoMapping` pair (e.g. `CustomerRefDto`
reached from many `Contact`s via a shared `Customer`, or `AddressDto` shared as `billingAddress`
across customers). A type that's only ever a top-level `mapTo(...)`/`mapList(...)` entry point can
never receive the same source instance twice within one call - Ebean's own query engine already
de-duplicates root entity instances - so the cache lookup/insert there is pure overhead with a
guaranteed-never-hit `IdentityHashMap`.

Since all `@DtoMapping` pairs are resolved together at codegen time (`DtoMappingReader.
resolveAndValidate()`), it's straightforward to compute this: after cycle exclusion, walk every
surviving `DtoBeanMeta`'s properties and mark any `nested()` target as `nestedElsewhere()`. The
generated `map()` method then branches per mapper:

```java
// CustomerDto - never nested by another mapper, only a mapTo()/mapList() entry point
public CustomerDto map(Customer source, DtoMapContext context) {
  if (source == null) return null;
  // DtoMapContext for nested mappers only
  return new CustomerDto(source.getId(), source.getName(),
    billingAddressMapper.map(source.getBillingAddress(), context),
    contactsMapper.mapList(source.getContacts(), context));
}

// AddressDto - nested under CustomerDto.billingAddress, so may be shared across customers
public AddressDto map(Address source, DtoMapContext context) {
  if (source == null) return null;
  // dedup using DtoMapContext, same Address instance can be reached via more than one path in the graph
  return context.computeIfAbsent(AddressDto.class, source, s -> new AddressDto(
    s.getId(), s.getLine1(), s.getCity()));
}

// ContactSummaryDto - flat, top-level only, no nested children at all
public ContactSummaryDto map(ContactSummary source, DtoMapContext context) {
  if (source == null) return null;
  // skip DtoMapContext, only ever a top-level mapping
  return new ContactSummaryDto(source.getId(), source.getFullName());
}
```

Deliberately terse, single-line comments - just enough for a developer skimming generated code (e.g.
per the earlier `@Formula2`-on-DTO worked example) to know at a glance *why* a given mapper does or
doesn't use the cache, without spelling out the full reachability argument inline every time (that
lives here in the design doc instead). Note `CustomerDto`'s own construction skips the cache even
though it *has* nested children - `context` is still threaded down to `billingAddressMapper`/
`contactsMapper` since those target types (`AddressDto`, `ContactDto`) *are* nested elsewhere and
still need the identity cache for themselves - hence the distinct "for nested mappers only" wording
from the "only ever a top-level mapping" case (`ContactSummaryDto`), which has no children to thread
a context to at all.

### Fetching a ToOne back-reference used only for its FK/id needs the FK property fetched too

Confirmed (via a first-cut test failure) that if a ToMany's element type has a ToOne back to its
parent (e.g. `Contact.customer`), that FK property must itself be included in the fetch
(`.fetch("contacts", "id,firstName,lastName,customer")`) even when the mapper only reads the id
off the reference. Omitting it throws `LazyInitialisationException: Property not loaded:
customer` on the `getCustomer()` call itself (not merely on a property access on the returned
reference) - i.e. the earlier "ToOne reference access alone doesn't lazy load" finding
(dto-validate-fetch-pagination) only holds once the ToOne/FK property is itself part of the
fetch/select spec. This reinforces r6 (auto-deriving the fetch spec from DTO shape): the codegen
must include a ToOne property in the fetch spec whenever a DTO needing it (even just its id) is
reachable through a ToMany, not just at the top level.

### Test coverage added

- `TestCustomerDtoGraphMapping` extended to cover `contacts` ToMany mapping and to assert that
  sibling `ContactDto`s under the same customer share the identical `CustomerRefDto` instance.
- `TestContactDtoGraphMapping` (new) - standalone `ContactDtoMapper` test focused specifically on
  the identity de-dup guarantee and null-source handling.

## Codegen foundation: avaje-prisms adopted in querybean-generator (dto-codegen-mapper, step 1)

Before writing the DTO-mapper annotation-processing logic itself, adopted `avaje-prisms`
(`io.avaje:avaje-prisms`) in `querybean-generator` as the mechanism for reading the new
`@DtoPath`/`@DtoRef` annotations at APT time, replacing what would otherwise be more hand-rolled
`AnnotationMirror` walking (the existing pattern in `FindDbName.java`/`ReadModuleInfo.java`,
left as-is/unmigrated - only the *new* annotations use prisms).

This mirrors the proven pattern already used in two sibling projects in the same ecosystem -
`avaje-inject`'s `inject-generator` and `avaje-jsonb`'s `jsonb-generator` - both declare
`@GeneratePrism(SomeAnnotation.class)` once and get a generated `SomeAnnotationPrism` with
`isPresent(element)` / `getInstanceOn(element)` / `getOptionalOn(element)` and typed accessors
for every annotation member (correctly handling `Class`-valued members, avoiding the classic
`MirroredTypeException` dance).

Key property preserved: `querybean-generator` has **zero runtime/compile dependencies today**
(confirmed via `mvn dependency:list` returning "none"), matching annotations by FQN string
constants (`Constants.java`) rather than importing the actual annotation classes - deliberately
keeping the processor free of any dependency footprint for consumers. Adding `avaje-prisms` (to
generate the prism wrapper) and `ebean-annotation` (to reference `@DtoPath`/`@DtoRef` as literal
`Class` values in `@GeneratePrism(...)`) as `optional` dependencies preserves this: `mvn
dependency:list -DincludeScope=runtime` confirms every one of these (plus their own transitive
deps: `avaje-prism-core`, `avaje-spi-service`, `avaje-spi-core`) is marked `(optional)`, so none
of it propagates to a project that depends on `querybean-generator` (whether as a normal
dependency or via `annotationProcessorPaths`).

New annotations were added to the separate `ebean-annotation` repo (`io.ebean.annotation`
package, alongside `@Formula2`), not this repo:

```java
@DtoPath("billingAddress.line1")
String billingLine1;   // rename/flatten a DTO property from a nested source path

@DtoRef
Integer customerId;    // id-only back-reference, breaks what would otherwise be a graph cycle
```

Both use `@Target({FIELD, METHOD})` and `RetentionPolicy.CLASS` - visible to the annotation
processor (including across module boundaries, since `CLASS` retention survives in the compiled
`.class` file) but absent from runtime reflection, consistent with DTOs remaining plain,
framework-free types with no runtime footprint.

Wiring changes in `querybean-generator`:
- `pom.xml`: added `avaje-prisms` (`optional`, plus `annotationProcessorPaths` entry) and
  `ebean-annotation` (`optional`) dependencies; removed the previous `-proc:none` compiler arg
  (which would have suppressed `avaje-prisms`' own processor from running to generate the prism
  source) - annotation processing is now scoped to exactly `avaje-prisms` via the explicit
  `annotationProcessorPaths` list, so no other processor is auto-discovered.
- `module-info.java`: added `requires static io.avaje.prism;` and `requires static
  io.ebean.annotation;` (`static` = compile-time only, matching the `optional` Maven scope).
- New `package-info.java` declaring `@GeneratePrism(DtoPath.class)` and
  `@GeneratePrism(DtoRef.class)`, generating `DtoPathPrism`/`DtoRefPrism` into
  `target/generated-sources/annotations`.

Verified: full `querybean-generator` build + existing test suite pass unchanged, and a downstream
full rebuild (`ebean-test` with `-am`) - which exercises the existing Q-bean codegen - also
passes with no regressions.

### Trigger mechanism: `@DtoMapping(source, target)` on a neutral package-info.java

Considered and rejected: putting a `source`/entity-referencing annotation directly on the DTO
class itself (e.g. `@Dto(Customer.class)` on `CustomerDto`). Rejected because DTO types are
often owned/generated elsewhere (e.g. from an OpenAPI spec) and must not be forced to reference
an internal persistence/entity type - that would leak internal domain types into a
public-facing/generated DTO module.

Instead, adopted the same pattern `avaje-jsonb` uses for external/foreign types it doesn't own
(`@Json.Import`): a repeatable annotation declared on a *neutral* holder - a `package-info.java`
- naming the `source` entity and `target` DTO as a pair:

```java
@DtoMapping(source = Customer.class, target = CustomerDto.class)
@DtoMapping(source = Contact.class, target = ContactDto.class)
package org.example.dto;
```

`@DtoMapping` (new, in `ebean-annotation`) is `@Target({PACKAGE, MODULE})`,
`@Retention(SOURCE)` (pure codegen trigger, never needed at runtime - unlike `@DtoPath`/
`@DtoRef` which need `CLASS` retention to remain visible to the DTO field itself),
`@Repeatable(DtoMapping.List.class)` following Java's own repeatable-annotation idiom. Neither
the entity nor the DTO needs any annotation of its own.

**Generated mapper package placement** - also modeled directly on `avaje-jsonb`'s handling of
`@Json.Import` for external types (`AdapterName`/`ProcessingContext.isImported`): defaults to the
target DTO's own package, *unless* the source or target type belongs to a different Java module
than the one being processed, in which case the generated mapper is placed in a package derived
from the processing module's own name instead - avoiding a JPMS "split package" violation that
would occur from generating source into a package owned by another module. An explicit
`mapperPackage` attribute is available to override this for edge cases. Same-module (or
non-modular/unnamed-module) projects are unaffected and just get the mapper alongside the DTO.

### mapTo(Class) dispatch: Class-token API + generated compile-time-safe registry

The original API sketch above (`mapTo(CustomerDto.class)`) predates the native-image/no-reflection
decision. Rather than switching to an instance-based API (`mapTo(new CustomerDtoMapper())`),
decided to keep the `Class`-token shape and generate a compile-time-safe registry to resolve it -
no reflection, no `Class.forName`, just literal `Class` comparisons generated at build time, e.g.:

```java
<S, D> DtoMapper<S, D> mapperFor(Class<S> sourceType, Class<D> targetType) {
  if (sourceType == Customer.class && targetType == CustomerDto.class) {
    return (DtoMapper<S, D>) new CustomerDtoMapper();
  }
  if (sourceType == Contact.class && targetType == ContactDto.class) {
    return (DtoMapper<S, D>) new ContactDtoMapper();
  }
  return null;
}
```

Dispatch is keyed on the **(source, target) pair**, not target alone - this matches how
`@DtoMapping(source, target)` pairs are declared, allows the same DTO type to be mapped from more
than one source entity without ambiguity, and lets `query.mapTo(dtoType)` fail fast with a clear
`PersistenceException` (rather than an incorrect match) when `query.getBeanType()` doesn't pair
with the requested DTO.

This mirrors the existing, already-proven `EbeanEntityRegister`/`EntityClassRegister` mechanism
(`SimpleModuleInfoWriter.java`) that `querybean-generator` already generates per module for entity
classes - a `List<Class<?>>` built from literal `SomeEntity.class` references, registered via
`META-INF/services` (`ServiceLoader`, itself native-image-friendly with no extra reflection
config needed for simple no-arg-constructor implementations). The DTO mapper registry follows the
same per-module aggregation + `META-INF/services` registration shape, giving `mapTo(Class)` a
concrete generated implementation to dispatch through at runtime without reflection anywhere in
the chain.

### mapTo(Dto.class) runtime wiring (implemented)

`query.mapTo(dtoType)` returns a `MappedQuery<D>` (`findList()`/`findOne()`/`findOneOrEmpty()`/
`findStream()`/`findPagedList()`/`usingMaster(boolean)`/`usingTransaction(Transaction)`/`usingConnection(Connection)`).
On first use it resolves the generated `DtoMapper<S, D>` for the query's `(getBeanType(), dtoType)`
pair via a `DtoMapperManager` (a `ServiceLoader`-backed aggregator over all generated
`DtoMapperRegister`s, analogous to `DtoBeanManager`), then:

- applies `mapper.fetchGroup()` to the query via `query.select(fetchGroup)` - the fetch/select spec
  is entirely derived from the DTO's declared shape, no manual `.select()`/`.fetch()` needed;
- forces `query.setUnmodifiable(true)` - the resulting entity graph is read-only input to the
  mapper, and any DTO property whose source wasn't actually fetched fails fast with
  `LazyInitialisationException` rather than silently lazy loading or returning `null`;
- executes the query and maps the result(s) via `mapper.map(...)`/`mapper.mapList(...)`.

An unregistered `(source, dtoType)` pair throws a `PersistenceException` with a suggested
`@DtoMapping` fix, at first use (i.e. `findList()`/`findOne()`), not at `mapTo(dtoType)` call time.

`MappedQuery<D>.usingMaster(boolean)`, `.usingTransaction(Transaction)`, and `.usingConnection(Connection)`
all delegate directly to the underlying entity query, mirroring `Query`/`QueryBuilder`. This lets a
caller retry against the master data source after a read-replica failure by calling
`usingMaster(true)` on the *same* `MappedQuery` instance and re-invoking a find method - there's no
need to rebuild the query and call `.mapTo(...)` again.

`MappedQuery<D>.findStream()` mirrors `QueryBuilder#findStream()` - the underlying entity query is
streamed (supporting very large result sets, potentially using multiple persistence contexts
internally) and each entity is mapped to its target DTO lazily as the stream is consumed. One
`DtoMapContext` is shared across the whole stream (not per-element), so identity de-duplication of
nested DTOs (e.g. several `Contact`s sharing the same `Customer`) still holds even when the source
entities are never materialized into one `List` at all. As with the entity-level `findStream()`,
callers must consume it via try-with-resources to ensure the underlying resources are closed.


## Still open / to revisit during implementation

- Whether `.fetch(...)` calls can still be layered on top of a `mapTo(Dto.class)` query for explicit
  overrides. Currently the mapper's `fetchGroup()` is the *only* source of the fetch spec - any
  `.select()`/`.fetch()` calls made before `.mapTo(...)` are overwritten by it.
- Whether `@DtoPath`/`@DtoRef` need additional attributes beyond a bare path/marker (e.g. an explicit
  target type on `@DtoRef` for disambiguation) once real DTOs with more complex shapes are codegen'd.
- Behavior when a DTO property has no matching entity property and no `@DtoPath`/`@Formula2` override
  (fail at codegen time, most likely, consistent with the "fail fast" philosophy).
- `@Formula2`-on-DTO mapping to Blaze-Persistence/QueryDSL-style computed properties - not yet
  implemented (see requirements doc); a narrower validation-only variant was attempted and rejected
  as not distinct enough from `@DtoPath` (see "Formula2-on-DTO scope" above). The broader ad-hoc-SQL
  case likely doesn't need a dedicated DTO feature at all - see "Ad-hoc computed/formula properties"
  above for the `@Entity @View`/`@Sql` alternative.
- **Fetch-path collision between a `NESTED_ONE`/`NESTED_MANY` property and a `@DtoPath` property -
  found and fixed**: `DtoMapperWriter.fetchGroupChainCalls()` builds one `.fetch(path, ...)`
  chain-call per distinct fetch path, but the underlying `OrmQueryDetail.fetch(...)` unconditionally
  **overwrites** (rather than merges) any existing entry for the same path key. If a DTO declared a
  `NESTED_ONE`/`NESTED_MANY` property AND a `@DtoPath` property whose fetch-path prefix is the *exact
  same* path (e.g. a nested `AddressDto billingAddress` alongside `@DtoPath("billingAddress.line1")`
  on the same DTO - both resolve to fetch path `"billingAddress"`), the generator would emit two
  `.fetch("billingAddress", ...)` calls and the second would silently discard the first's selected
  properties. Merging wasn't practical - the nested property's `.fetch(path, mapper.fetchGroup())`
  call passes another mapper's own pre-built, immutable, shared `FetchGroup`, so there's no clean way
  to splice an extra scalar property into it at the call site. Fixed instead with a **fail-fast
  compile-time error**: `DtoMapperWriter` now detects the collision and raises a clear
  `ctx.logError(...)` (annotation-processor `ERROR` diagnostic, fails the compile) naming the
  colliding property and fetch path, and suggesting the two ways out - move the property onto the
  nested DTO type instead, or pick a `@DtoPath` that reaches a different, non-colliding path (as
  `ContactDto.customerCity` already does deliberately, per its own comment, using a 3-segment path).
  Verified empirically by compiling a small reproduction with a colliding `@DtoPath` and confirming
  the expected error fires; a permanent regression test
  (`DtoMapperFetchPathCollisionTest` in `querybean-generator`) now runs this same repro directly
  through `javax.tools.JavaCompiler` with the `Processor` registered, asserting the compile fails with
  the expected diagnostic message.
- **Compile-time verification of `select(...).asDto(...)` (r6, aspirational) - explored and closed as
  rejected**: raw SQL is an opaque `String` at compile time, and even the typed query-bean
  `.select(...)` form only type-checks against the *entity* - the match to the target DTO's constructor
  still happens at runtime via reflection (`DtoQueryPlanConstructor`), and the `.asDto(...)` call site
  can be arbitrarily distant from the `.select(...)` call, so there's no fixed AST shape an annotation
  processor could reliably verify (unlike QueryDSL, whose compile-time safety actually comes from typed
  `Projections.constructor(...)`/generated Q-type constructor calls, not from checking a select-list
  against a DTO). `mapTo(Dto.class)` already closes the underlying gap in the tractable direction - it
  derives the select/fetch spec *from* the DTO's declared shape at APT time, so it is compile-time safe
  by construction. Recommend `mapTo()` whenever compile-time-checked DTO projection matters, and treat
  `asDto()`/`findDto()` as the flexible, runtime-checked escape hatch for raw/dynamic SQL. See
  `dto-mapping-requirements.md` requirement r6.
- **Custom property conversion (`@DtoConvert`/`@DtoMixin`, r13/r14) - implemented**: motivated by a
  real hand-written mapper (`DriverMapper`, central-access) needing both a dependency-free scalar
  coercion (`short` -> `boolean`) and a dependency-backed conversion (AES decryption via an injected
  cipher). Final design (see `dto-mapping-requirements.md` section E), as built:
  - `@DtoConvert(value = ConverterType.class, method = "name")` on a DTO property (combinable with
    `@DtoPath`); the generator dispatches on whether the referenced method is `static` - static means a
    direct inlined static call (no registration, covers common reusable coercions), instance means
    dispatch via a new `DtoConverterManager.get(ConverterType.class).method(...)` call, with the
    resolved instance wired as a real constructor parameter/field on the generated mapper (same shape
    as existing nested-mapper constructor injection). Multiple properties on the same mapper sharing
    the same converter type are deduplicated to a single constructor parameter/field
    (`DtoBeanMeta.converterDeps()`).
  - `DtoConverterManager` (`ebean-api`, `io.ebean` package) is a small, narrowly-scoped static put/get
    bridge - the app registers an already-DI-constructed converter singleton (e.g. built by
    avaje-inject) *before* building the `Database`. This is a deliberate, narrow exception to the
    general no-static-mutable-state convention: `ServiceLoader`-discovered, no-arg-constructed
    generated code (`EbeanDtoMapperRegister`) has no other way to reach an already-DI-constructed
    singleton. `DtoConverterManager.get(type)` throws immediately if nothing was registered for that
    type, so a missing converter fails fast at Database-startup time (an eager field initializer on
    `EbeanDtoMapperRegister`, and equally on each mapper's own no-arg constructor, which resolves the
    same way via `DtoConverterManager.get(...)` for standalone/test construction), not lazily on first
    use - `DtoMapperRegister`'s `mapperFor(...)` signature and `DtoMapperManager` are otherwise
    completely unchanged, as originally planned.
  - Two alternatives were explored and rejected first: (a) a `DtoMapContext.service(Class)` lookup -
    wrong lifetime, `DtoMapContext` is a short-lived per-call identity-cache only; (b) a
    `ServiceLoader`-discovered `DtoConverterSource` SPI mirroring `DtoMapperRegister` itself - can't
    bridge to an *already* DI-constructed dependency without reconstructing/duplicating it.
  - `@DtoMixin(Target.class)` - a companion type overlaying `@DtoPath`/`@DtoConvert`/`@DtoRef`
    annotations onto a DTO that can't be annotated directly (e.g. OpenAPI-generated). Discovered via
    `roundEnv.getElementsAnnotatedWith(...)` (added to `Processor.getSupportedAnnotationTypes()`,
    since - unlike `@DtoPath`/`@DtoRef`/`@DtoConvert` - a mixin doesn't annotate an already-iterated
    field of a known `@DtoMapping` target, so it can't be found lazily). `DtoMappingReader` resolves
    each target property's annotations from the field itself first, falling back to a same-named
    method on the registered mixin (`DtoMappingReader.prismOn(...)`) - directly mirrors avaje-jsonb's
    proven `@Json.MixIn` mechanism.
  - Implemented in `ebean-annotation` (`DtoConvert`, `DtoMixin`), `ebean-api` (`DtoConverterManager`),
    and `querybean-generator` (`DtoConverterMeta`, `DtoBeanMeta.converterDeps()`,
    `DtoMappingReader`/`DtoMapperWriter`/`DtoMapperRegisterWriter` changes). Test coverage:
    `tests/test-dto-mapping` `TestDtoConvert` (static + instance dispatch, fail-fast unregistered-type
    check) and `TestDtoMixin` (mixin overlay, including instance-dispatch conversion resolved purely
    from mixin-declared annotations). The instance-dispatch converter is registered via a
    `DatabaseConfigProvider` (ServiceLoader hook run before the `Database` is built) rather than a test
    `@BeforeAll`, since `EbeanDtoMapperRegister`'s mapper fields (including any needing
    `DtoConverterManager`) are all constructed eagerly during `Database` startup, which can be
    triggered by whichever test class in the module happens to run first.

- **Fixed (validation phase, found via `central-access`): `@DtoPath` through a computed/derived
  getter now fails at compile time, with an explicit `requires()` escape hatch.** `@DtoPath`
  assumes every dotted segment names a real, fetchable Ebean bean property - so a path like
  `@DtoPath("currentMachine.organisationMachine.registrationPlate")`, where `getOrganisationMachine()`
  is a hand-written derived getter (not a real relation/column), used to **compile cleanly** (the
  codegen had no way to tell it apart from a real property from source alone) but **fail at
  runtime** with a `PersistenceException: No property found for [organisationMachine] in
  expression ...`, because the generated `FetchGroup` builder tried to `fetch`/`select` it as if it
  were a real Ebean property.
  - Two genuinely separate sub-problems: (1) *detecting* that a path segment isn't a real,
    fetchable property - solvable at compile time, since a real persistent property always has a
    backing field (Ebean requires one to enhance), checked via `javax.lang.model`
    (`ElementFilter.fieldsIn(...)` over the type + superclass chain, see `DtoMappingReader.hasField(...)`);
    versus (2) *knowing what the computed getter needs fetched* to execute safely - not solvable at
    compile time without full static/bytecode analysis of the getter's method body, out of scope.
  - Resolution: don't attempt to infer (2) automatically. When `DtoMappingReader` detects a `@DtoPath`
    segment with no backing field, it now fails fast at compile time (`ctx.logError(...)`) unless the
    developer explicitly declares the real entity paths that must be fetched via
    `@DtoPath(requires = {...})` (dot-notation, same convention as `@DtoPath`'s own `value()`) - e.g.
    `@DtoPath(value = "primaryContact.lastName", requires = "contacts")` where `getPrimaryContact()`
    picks the first entry out of the `contacts` collection. The real prefix before the computed
    segment (if any) is automatically combined with the declared `requires()` paths, so the developer
    doesn't need to redundantly repeat it. Declared paths are emitted as bare `.fetch(path)` calls in
    the generated `FetchGroup` (distinct from the `.fetch(path, "props")` shape used for ordinary
    scalar `@DtoPath` properties, since there's no specific target property list to narrow to here).
  - **The zero-extra-fetch case is also supported, via an explicit `requires = {}`** - e.g.
    `@DtoPath(value = "idBadge", requires = {})` where `getIdBadge()` derives purely from `id`
    (always fetched regardless). An explicit empty array confirms "nothing extra needed", distinct
    from omitting `requires()` entirely ("not yet considered", still a compile error) - `requires()`
    itself can't tell the two cases apart (both read back as an empty `List`), so `DtoMappingReader`
    checks the avaje-prism-generated `DtoPathPrism.values.requires()` instead, which returns `null`
    only when the member was left at its default (i.e. omitted from source). `DtoPropertyMeta`
    correspondingly carries `hasComputedSegment()` as its own boolean flag (set whenever a computed
    segment was detected at all), independent of whether `requiredFetchPaths()` happens to be empty -
    an earlier version conflated the two (inferring "has a computed segment" from "has a non-empty
    requiredFetchPaths list"), which broke exactly this explicit-empty case by falling through to the
    ordinary scalar `.select(...)` path and failing at runtime with `PersistenceException: Property
    not found - idBadge` (`idBadge` isn't a real Ebean property, so it can't be selected).
  - Implemented in `ebean-annotation` (`DtoPath.requires()`), and `querybean-generator`
    (`DtoMappingReader` computed-segment detection/validation, `DtoPropertyMeta.requiredFetchPaths()`/
    `hasComputedSegment()`, `DtoMapperWriter.fetchGroupChainCalls()` bare-fetch emission). Test
    coverage: `tests/test-dto-mapping` `ComputedPathDto`/`TestComputedPath` (happy path, `requires`
    correctly fetches the dependency and the mapped value is correct), `ComputedPathNoFetchDto`/
    `TestComputedPathNoFetch` (explicit `requires = {}`, genuinely nothing extra needed), and
    `querybean-generator`'s `DtoMapperComputedPathTest` (negative case - omitting `requires` on a
    computed segment is a compile-time `ERROR` diagnostic, verified via direct `javax.tools.JavaCompiler`
    compilation, mirroring `DtoMapperFetchPathCollisionTest`).
  - Known gap: the dedup between the computed segment's required fetch paths and existing
    `pathSelect`/`nestedAssocPaths` keys in `DtoMapperWriter` is a simplified exact-path-string check
    (skip emitting a duplicate `.fetch(path)`), not full collision detection like the existing
    NESTED_ONE/MANY vs `@DtoPath` check - a bare `fetch(path)` and an existing `fetch(path,
    "specific,props")` for the same path string are not merged/reconciled, just left as two separate
    calls if that edge case arises.

## References

- Requirements: [dto-mapping-requirements.md](./dto-mapping-requirements.md)
- Issue: https://github.com/ebean-orm/ebean/issues/2540
- MapStruct cycle mapping: https://mapstruct.org/documentation/stable/reference/html/#mapping-object-cycles
