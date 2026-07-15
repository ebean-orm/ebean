# Guide: Mapping entity graphs to DTOs — `query.mapTo(Dto.class)`

## Purpose

`query.mapTo(SomeDto.class)` maps an entity query result to a **nested DTO graph** —
DTO fields can themselves be DTOs (`ToOne`) or `List<Dto>`/`Set<Dto>` (`ToMany`), not
just flat scalar columns. Ebean generates the mapper (reflection-free), automatically
derives the query's `select()`/`fetch()` spec from the target DTO's declared shape, and
forces `setUnmodifiable(true)` so any property the mapper needs but wasn't fetched fails
fast with `LazyInitialisationException` instead of silently lazy loading.

This is distinct from the existing flat `asDto(Dto.class)` — see
[Quick comparison](#quick-comparison-mapto-vs-asdto-vs-plain-entity-query) below.

```java
Optional<CustomerDto> dto = new QCustomer()
  .id.eq(customerId)
  .mapTo(CustomerDto.class)
  .findOneOrEmpty();
```

```java
List<CustomerDto> dtos = DB.find(Customer.class)
  .where().eq("status", Status.ACTIVE)
  .mapTo(CustomerDto.class)   // no .select()/.fetch() needed - derived from CustomerDto's shape
  .findList();
```

---

## Quick comparison: `mapTo()` vs `asDto()` vs plain entity query

| | `mapTo(Dto.class)` | `asDto(Dto.class)` | Plain entity query |
|---|---|---|---|
| Shape | Nested DTO **graph** (ToOne/ToMany) | Flat, single-row DTO | Entity graph |
| Fetch spec | Auto-derived from the DTO's declared shape | Whatever `select()`/SQL you write | Whatever `select()`/`fetch()` you write |
| Mismatch caught | At compile time (unregistered pair fails fast at first use; codegen fails fast on structural problems) | At runtime (reflection-based constructor/setter matching) | N/A (real entity properties) |
| Identity/de-dup | Yes - repeated source instances map to the same DTO instance (`DtoMapContext`) | N/A (one row in, one DTO out) | Yes (entity/persistence-context identity) |
| Backing pipeline | Executes the entity ORM query, `setUnmodifiable(true)`, maps the resulting graph | Executes SQL directly against a flat `ResultSet` | Executes the entity ORM query |
| Best for | API/read-model responses that mirror a **nested** entity shape | Flat summary rows, reports, native/vendor SQL | Data you intend to mutate and save back |

See also [writing-ebean-query-beans.md](writing-ebean-query-beans.md) (Step 8/9) for
`asDto()` and the general query-shape decision guide.

---

## Basic usage

### 1. Declare a plain DTO

DTOs are plain classes with **no framework attachment** — no annotations required for
the common case (properties matched to the source entity by name):

```java
public class CustomerDto {
  private final Long id;
  private final String name;
  private final AddressDto billingAddress;   // nested ToOne
  private final List<ContactDto> contacts;   // nested ToMany

  public CustomerDto(Long id, String name, AddressDto billingAddress, List<ContactDto> contacts) {
    this.id = id;
    this.name = name;
    this.billingAddress = billingAddress;
    this.contacts = contacts;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public AddressDto getBillingAddress() { return billingAddress; }
  public List<ContactDto> getContacts() { return contacts; }
}
```

A constructor whose parameters match (by name) a source entity/DTO property is used
for mapping — same shape convention as the existing `DtoQuery`. Getters are used to
read the source's properties — a bare/fluent accessor like `active()` is resolved
automatically too, not just `getActive()`/`isActive()` (useful both for Ebean's own
record entity beans and for ordinary classes that just expose bare-name accessors).

### 2. Register the (source, target) pair

Declare each entity → DTO pair with `@DtoMapping` on a `package-info.java` (a neutral
holder — see [Why `package-info.java`?](#why-package-infojava)):

```java
@DtoMapping(source = Customer.class, target = CustomerDto.class)
@DtoMapping(source = Address.class, target = AddressDto.class)
@DtoMapping(source = Contact.class, target = ContactDto.class)
package org.example.dto;

import io.ebean.annotation.DtoMapping;
```

This triggers `querybean-generator` (the existing annotation processor) to generate a
`CustomerDtoMapper implements DtoMapper<Customer, CustomerDto>` for each pair — no new
Maven/Gradle setup beyond what query beans already require.

### 3. Query with `mapTo(...)`

```java
List<CustomerDto> dtos = DB.find(Customer.class)
  .where().eq("status", Status.ACTIVE)
  .mapTo(CustomerDto.class)
  .findList();

CustomerDto one = new QCustomer().id.eq(id).mapTo(CustomerDto.class).findOne();

Optional<CustomerDto> maybe = new QCustomer().id.eq(id).mapTo(CustomerDto.class).findOneOrEmpty();
```

`mapTo(...)` works the same from a query bean (`QCustomer`) or a plain `DB.find(...)`/
`ExpressionList` query.

### Paging - `findPagedList()`

`findPagedList()` mirrors `Query#findPagedList()` — the underlying entity query is paged
as normal and each page's result is mapped to the target DTO list:

```java
PagedList<CustomerDto> paged = DB.find(Customer.class)
  .where().eq("status", Status.ACTIVE)
  .orderBy().asc("name")
  .setFirstRow(0)
  .setMaxRows(50)
  .mapTo(CustomerDto.class)
  .findPagedList();

int totalRowCount = paged.getTotalCount();      // page metadata - unaffected by DTO mapping
List<CustomerDto> page1 = paged.getList();       // mapped DTOs for this page
```

Page metadata (`getTotalCount()`, `getTotalPageCount()`, `hasNext()`, `hasPrev()`,
`loadCount()`, ...) reflects the underlying entity query directly; only `getList()`
is mapped (once, cached) to the DTO type.

### An unregistered pair fails fast

If `(Customer.class, SomeDto.class)` was never declared via `@DtoMapping`, the first
`mapTo(SomeDto.class)` call throws immediately:

```
PersistenceException: No DtoMapper registered mapping Customer -> SomeDto
  - check @DtoMapping(source = Customer.class, target = SomeDto.class) is declared
    on a package-info.java processed by querybean-generator
```

---

## Auto-derived fetch spec

You never write `.select()`/`.fetch()` for a `mapTo(...)` query — the generated mapper
exposes a `fetchGroup()` built directly from the DTO's declared shape, and `mapTo(...)`
applies it automatically:

```java
public CustomerDtoMapper() {
  this(new AddressDtoMapper(), new ContactDtoMapper());
}

public CustomerDtoMapper(DtoMapper<Address, AddressDto> billingAddressMapper,
                          DtoMapper<Contact, ContactDto> contactsMapper) {
  this.fetchGroup = FetchGroup.of(Customer.class)
    .select("id,name")
    .fetch("billingAddress", billingAddressMapper.fetchGroup())
    .fetch("contacts", contactsMapper.fetchGroup())
    .build();
}
```

Each nested DTO gets its own generated mapper (mirroring MapStruct's per-type mapper
generation), wired together via constructor injection — mappers are stateless and
substitutable, not static singletons. Mapper instances are constructed once, in
dependency order, and reused — see [DtoMapperManager](#one-mapper-instance-per-pair)
below.

---

## Nested collections and identity-aware de-duplication

When the same source entity instance is reachable via more than one path in the graph
(e.g. two `Contact`s sharing the same `Customer`, or the same `Address` referenced from
two paths), the mapper reuses the **same** target DTO instance rather than creating
duplicate-but-equal copies — mirroring the identity semantics the entity graph already
has:

```java
List<CustomerDto> dtos = DB.find(Customer.class).mapTo(CustomerDto.class).findList();

CustomerDto customer = dtos.get(0);
// both contacts share the exact same customer.billingAddress AddressDto instance
assertThat(customer.getContacts().get(0).getCustomer())
  .isSameAs(customer.getContacts().get(1).getCustomer());
```

This is done via a `DtoMapContext` threaded through every nested `map(...)` call within
one top-level `mapList(...)`/`findList()` invocation. The generated code only pays for
this when it can actually matter — a DTO that's never nested under another DTO skips
`DtoMapContext` entirely (there's nothing else in scope to de-duplicate against):

```java
// AddressDto is nested under CustomerDto (reachable via multiple contacts) - dedup needed
// dedup using DtoMapContext, same Address instance can be reached via more than one path in the graph
return context.computeIfAbsent(AddressDto.class, source, s -> new AddressDto(...));

// ContactSummaryDto is only ever mapped as a top-level query result - no dedup possible
// skip DtoMapContext, only ever a top-level mapping
return new ContactSummaryDto(source.getId(), source.getFullName());

// CustomerDto has nested mappers (billingAddress, contacts) but is never itself nested
// DtoMapContext for nested mappers only
return new CustomerDto(source.getId(), source.getName(), ...);
```

The generated comment tells you at a glance which of the three cases applies — useful
when debugging why a `DtoMapContext` is (or isn't) in the generated code for a
particular mapper.

---

## Using generated mappers directly (outside `query.mapTo()`)

Every generated `XxxDtoMapper` is a plain public class — you don't need `ServiceLoader`,
a registry, or a `Database` just to construct or call one directly (though
`DtoMapperManager`, below, is available if you want a shared, DI-friendly lookup). It
always has a public no-arg constructor (delegating to defaults for any nested mappers/
`@DtoConvert` converters) plus an explicit constructor taking those dependencies directly,
and implements `DtoMapper<SOURCE, TARGET>`'s `map(...)`/`mapList(...)`:

```java
CustomerDtoMapper mapper = new CustomerDtoMapper();
CustomerDto dto = mapper.map(customer);       // any Customer you already have on hand
List<CustomerDto> dtos = mapper.mapList(customers);
```

This works on **any** entity graph, not just one that just came out of a `mapTo(...)`
query — e.g. entities you loaded with a plain `.fetch(...)` query, entities you just
`.save()`d, or entities built by hand in a test. The only requirement is that whatever the
mapper reads (via plain getters) is actually populated — there's no lazy-loading fallback.

### Testing the mapping in isolation

Because mappers are plain, constructor-injected classes, you can unit test the mapping
logic itself — independent of `query.mapTo()`, the DTO-pair registry, and (for
`@DtoConvert` instance-dispatch converters) `DtoConverterManager` — by passing a test
double straight into the explicit constructor:

```java
SecretCipher upperCasingTestCipher = String::toUpperCase;
ContactConversionDto dto = new ContactConversionDtoMapper(upperCasingTestCipher).map(contact);

assertThat(dto.getSecretCode()).isEqualTo("SHH");
```

No `DtoConverterManager.put(...)` registration needed for this kind of test — the real
production wiring (`DtoConverterManager.get(SecretCipher.class)`) only happens in the
generated no-arg constructor, which the explicit-constructor call above bypasses entirely.
See `TestCustomerDtoGraphMapping` (mapper called directly against a manually queried
graph) and `TestMapperManualUsage` (mapper called directly against hand-built/just-saved
entities, plus the converter test-double case above) in `tests/test-dto-mapping`.

### `DtoMapperManager` — resolving a generated mapper for dependency injection

`new CustomerDtoMapper()` is enough for a single mapper, but if your application wants a
single shared instance of *every* generated mapper (mirroring how `query.mapTo()` resolves
them internally) - e.g. to wire one up for constructor injection into a service, replacing
a hand-written mapper class - use `io.ebean.DtoMapperManager`:

```java
DtoMapperManager manager = new DtoMapperManager();   // ServiceLoader discovery only - no Database needed
CustomerDtoMapper mapper = manager.get(CustomerDtoMapper.class);
```

`DtoMapperManager` has no dependency on `Database` at all - its constructor only does
`ServiceLoader.load(DtoMapperRegister.class)` - so it can be constructed independently,
before (or entirely without) a `Database`, e.g. as a bean in an avaje-inject (or any DI
framework's) dependency graph:

```java
@Factory
class DtoMapperFactory {

  @Bean
  DtoMapperManager dtoMapperManager() {
    return new DtoMapperManager();
  }

  @Bean
  CustomerDtoMapper customerDtoMapper(DtoMapperManager manager) {
    return manager.get(CustomerDtoMapper.class);
  }
}
```

If you also want `query.mapTo(...)` to use that *exact same* manager instance (so there's
only ever one instance of each generated mapper, whichever path resolves it), register it
via `DatabaseBuilder.putServiceObject` before building the `Database` - this is the same
`putServiceObject`/`getServiceObject` mechanism already used for things like
`AutoMigrationRunner`:

```java
DtoMapperManager sharedManager = new DtoMapperManager();

Database db = Database.builder()
  .putServiceObject(DtoMapperManager.class, sharedManager)
  .build();

// query.mapTo(...) against `db` now resolves mappers via `sharedManager`
```

If nothing is registered via `putServiceObject`, the `Database` builds its own default
`DtoMapperManager` instance instead - registering one is entirely optional. A standalone
`DtoMapperManager()` construction bypasses the `DatabaseConfigProvider` hook (that hook is
specifically about `Database` startup ordering), so if any of your mappers need a
`@DtoConvert` instance-dispatch converter, register it via `DtoConverterManager.put(...)`
yourself first, exactly as you would before building a `Database`. See
`TestDtoMapperManager` and `TestDtoMapperManagerSharing` in `tests/test-dto-mapping`.

### Recipe: adding extra caller-supplied fields after mapping

Sometimes a target DTO needs a field that isn't sourced from the entity graph at all - e.g.
populated from a separate query or business rule, only when a caller-supplied flag is set.
Rather than the generator supporting partial/builder-based mapping directly, if your DTO is
a record with a "seed from instance" builder (e.g. via `avaje-recordbuilder`'s
`@RecordBuilder`, which generates `Target.builder(existingInstance)`), just map the
graph-sourced fields as usual and layer the extra field on afterwards:

```java
Driver base = mapper.map(cDriver);
Driver full = DriverBuilder.builder(base).fleets(fleets).build();
```

No generator changes needed - the mapped instance is simply the seed for the builder.

---

## Large targets: builder-based construction and named variants

Two features aimed at large, builder-shaped target DTOs (typically OpenAPI-generated records
with a generated builder), where a positional constructor call is unwieldy and a single query
needs to populate the target in more than one shape.

### Builder-based construction (`builder = AUTO | ALWAYS | NEVER`)

If the target has a static no-arg `Target.builder()` factory returning a type with a fluent
(returns-itself) setter per property plus a `build()` method - the shape
`avaje-recordbuilder`'s `@RecordBuilder` generates - the generated mapper can construct the
target via `Target.builder().prop(x)....build()` instead of `new Target(a, b, c, ...)`:

```java
public record User(Long id, String name, String email, /* ... 21 more fields */) {

  public static UserBuilder builder() {
    return UserBuilder.builder();
  }
}
```

```java
@DtoMapping(source = CUser.class, target = User.class)
package org.example.dto;
```

By default (`builder = AUTO`), the generator auto-detects a matching builder and uses it only
once the target has more than 5 properties, falling back to a positional constructor for
smaller DTOs. Override explicitly either direction:

```java
@DtoMapping(source = CUser.class, target = User.class, builder = DtoMapping.Builder.ALWAYS)
```

`builder = ALWAYS` is a codegen-time error if no matching builder shape is found; `builder =
NEVER` always uses a positional constructor even if a builder is detected. This applies
regardless of whether the target is hand-authored or foreign/generated - `@DtoMapping` is
already declared externally via `package-info.java`, so no annotation on the target itself is
needed either way.

### Named variants excluding nested paths (`name=`, `exclude=`)

The same `(source, target)` pair can be registered more than once - one base mapping (leaving
`name()` empty) plus any number of named variants, each excluding one or more nested
ToOne/ToMany properties:

```java
@DtoMapping(source = CUser.class, target = User.class)
@DtoMapping(source = CUser.class, target = User.class, name = "noFleets", exclude = "fleets")
package org.example.dto;
```

Both variants are generated into the **same** mapper class (one class per target, not one per
variant) - the generated `noFleets()` accessor returns a single shared/cached `DtoMapper<CUser,
User>` view (not reconstructed per call), omitting `fleets` from both its mapped output (`null`
for a ToOne, `List.of()` for a ToMany) and its own `fetchGroup()`. Each excluded property is still
evaluated inline at its own declared field position internally (guarded by a boolean flag) - a
variant's exclusions never change the evaluation order of the DTO's other properties. Select it
with the `query.mapTo(Class, DtoMapper)` overload, which takes an already-resolved mapper instance
directly - no string-based lookup:

```java
UserMapper userMapper = new UserMapper();

// full shape, with fleets fetched/mapped
List<User> withFleets = DB.find(CUser.class)
  .mapTo(User.class, userMapper) // or plain .mapTo(User.class)
  .findList();

// bulk listing shape - fleets excluded from both the fetch spec and the output
List<User> noFleets = DB.find(CUser.class)
  .mapTo(User.class, userMapper.noFleets())
  .findList();
```

Only nested ToOne/ToMany properties can be excluded - a scalar or `@DtoRef` property can't be,
since there's no type-safe "absent" value for an arbitrary scalar type. Named variants are
scoped to independent, top-level query results only - unlike the base mapping, they don't
participate in `DtoMapContext` identity de-duplication when nested elsewhere in a graph, since a
variant is never intended to be nested inside another DTO's mapping.

---

## `@DtoPath` — renamed or flattened properties

By default a DTO property is matched to the source entity property (or nested DTO
mapper) of the **same name**. `@DtoPath` overrides that, allowing a DTO property to be
renamed and/or flattened from a nested path using dot-notation:

```java
public class ContactDto {
  private final long id;
  private final String firstName;
  private final String lastName;

  @DtoPath("customer.billingAddress.city")
  private final String customerCity;   // flattened, 2 hops through customer

  // constructor / getters ...
}
```

The generated mapper reads the path with a null-guard at each hop and adds the
necessary joins to the fetch spec automatically:

```java
(s.getCustomer() == null ? null
  : (s.getCustomer().getBillingAddress() == null ? null
      : s.getCustomer().getBillingAddress().getCity()))
```

`@DtoPath` is purely a compile-time/codegen-time hint — the DTO class itself carries no
runtime dependency on the annotation.

### Fetch-path collisions are a compile-time error

A `@DtoPath` whose fetch path is identical to a nested `ToOne`/`ToMany` property's own
fetch path on the *same* DTO (e.g. a nested `customer` field alongside
`@DtoPath("customer.name")` — both resolve to fetch path `"customer"`) fails the build
with a clear error, rather than silently discarding one side's fetched properties:

```
error: @DtoPath property 'customerName' on FooDto resolves to fetch path 'customer',
  which collides with the nested mapping already using that same fetch path - Ebean's
  fetch spec can only carry one set of properties per path, so one silently discards
  the other. Move 'customerName' onto the nested DTO type instead, or choose a
  @DtoPath that reaches into a different, non-colliding path.
```

Fix it either way it suggests: move the property onto the nested DTO type, or choose a
`@DtoPath` that reaches a different path (as `customerCity` above does deliberately,
using a 3-segment path through `customer.billingAddress` rather than colliding with a
plain `customer` nested field).

---

## `@DtoRef` — id-only back-references (breaking cycles)

The DTO graph derived from a set of DTO types must form a DAG — codegen fails if it
doesn't. `@DtoRef` is the explicit escape hatch for an intentional back-reference, e.g.
a `Contact` DTO referencing its parent `Customer` by id only, rather than re-embedding
a full `CustomerDto` (which would recreate the `Customer → Contact → Customer` cycle):

```java
public class ContactDto {
  private final long id;

  @DtoRef
  private final Long customerId;   // id-only, no nested CustomerDto re-embedded

  // constructor / getters ...
}
```

The generated fetch spec adds the association to the **root** `select(...)` rather
than a nested `.fetch(...)` — this reads the foreign-key column directly off the base
table (no SQL join):

```java
this.fetchGroup = FetchGroup.of(ContactStats.class)
  .select("customer,contactCount,engagementScore")   // "customer" -> FK column, no join
  .build();
```

```java
(source.getCustomer() == null ? null : source.getCustomer().getId())
```

If the same association is *also* independently nested-fetched elsewhere on the DTO
(e.g. `ContactDto` has both a nested `customer` field **and** `@DtoRef Long
customerId`), the generator recognizes the association is already covered and doesn't
add a redundant/duplicate select — no join is added twice.

---

## `@DtoConvert` — custom property conversion

Some properties need more than a plain getter copy — a scalar coercion (`short` to
`boolean`), an enum-to-`String` mapping, or a conversion needing a real dependency (e.g.
decrypting a value with a cipher). `@DtoConvert(value = ConverterType.class, method =
"name")` covers both, combinable with `@DtoPath` when the source value also needs a
path/rename override:

```java
public class ContactDto {
  @DtoPath("status")
  @DtoConvert(value = ContactConversions.class, method = "toActive")
  private final boolean active;             // Contact.status (Short) -> boolean

  @DtoConvert(value = SecretCipher.class, method = "decode")
  private final String secretCode;          // decrypted via a registered SecretCipher

  // constructor / getters ...
}
```

The generator resolves the referenced method at codegen time and dispatches one of two
ways, purely based on whether it's `static`:

- **Static method** — inlined as a direct static call
  (`ContactConversions.toActive(source.getStatus())`). No registration needed at all —
  use this for common, reusable, dependency-free coercions.
- **Instance method** — the generated mapper resolves one shared instance via
  `DtoConverterManager.get(SecretCipher.class)`, wired as a constructor
  parameter/field (the same shape as nested-mapper constructor injection), then calls
  `secretCipher.decode(source.getSecretCode())`. Use this when the conversion needs a
  real dependency.

### Registering an instance-dispatch converter

`DtoConverterManager` is a small, deliberately-scoped static put/get bridge — register
an already-constructed converter instance (e.g. built by your DI container) **before**
building the `Database`:

```java
AES256Cipher cipher = ...;                                    // already DI-constructed
DtoConverterManager.put(SecretCipher.class, cipher::decrypt); // or a small adapter class

Database db = DatabaseFactory.create(...); // generated mappers resolve converters from here
```

If nothing is registered for a required type, `DtoConverterManager.get(...)` throws a
`PersistenceException` immediately — this happens as an eager field initializer on the
generated `EbeanDtoMapperRegister`, so a missing registration fails fast at `Database`
build time, not lazily on first `mapTo(...)` call.

> **Testing tip:** since `EbeanDtoMapperRegister`'s mapper fields are all constructed
> together when the `Database` starts, register converters via a `DatabaseConfigProvider`
> (a `ServiceLoader` hook that runs before the `Database` is built) rather than a test
> `@BeforeAll`, so registration always happens before *any* test triggers startup —
> regardless of which test class runs first.

## `@DtoMixin` — overlaying annotations onto a DTO you can't edit

Some DTOs are generated elsewhere (e.g. from an OpenAPI spec, regenerated on every
build) and can't be annotated directly. `@DtoMixin(Target.class)` overlays
`@DtoPath`/`@DtoRef`/`@DtoConvert` from a separate companion type instead — directly
mirroring avaje-jsonb's `@Json.MixIn` mechanism. Declare a companion interface (or
class) whose method names match the target DTO's property names:

```java
// ContactMixinDto itself carries no Ebean annotations at all
public class ContactMixinDto {
  public ContactMixinDto(long id, String firstName, boolean active, String secretCode) { ... }
  // getters ...
}

@DtoMixin(ContactMixinDto.class)
interface ContactMixinDtoMixin {

  @DtoPath("status")
  @DtoConvert(value = ContactConversions.class, method = "toActive")
  boolean active();

  @DtoConvert(value = SecretCipher.class, method = "decode")
  String secretCode();
}
```

The processor matches each mixin method to the target's property by name and applies
whichever annotations are present as if they were declared on the target field itself.
The mixin type is never instantiated and carries no runtime footprint — it's purely a
compile-time/codegen-time hint.

---

## Computed / aggregate properties via `@Entity @View`

There's no dedicated "formula on DTO" annotation (a narrower `@Formula2`-on-DTO
variant was explored and rejected — see
[dto-mapping-design.md](../dto-mapping-design.md) for the reasoning). Instead, model
the computed value as its own read-only entity using `@View`, then map that entity to a
plain DTO with the same `@DtoMapping` machinery described above. `@View(name = "...")`
here just points a second entity at an **existing** table — it does not create a new
database view or table.

### Worked example — computed column (`@Formula2`)

```java
@Entity
@View(name = "contact")   // reads the existing 'contact' table, no new DDL
public class ContactSummary {
  @Id
  private Long id;
  private String firstName;
  private String lastName;

  @Formula2("concat(firstName, ' ', lastName)")
  private String fullName;

  // getters ...
}
```

```java
public class ContactSummaryDto {
  private final Long id;
  private final String fullName;
  // constructor / getters ...
}
```

```java
@DtoMapping(source = ContactSummary.class, target = ContactSummaryDto.class)
```

```java
List<ContactSummaryDto> summaries = DB.find(ContactSummary.class)
  .mapTo(ContactSummaryDto.class)
  .findList();
```

### Worked example — group-by aggregation (`@Sum`/`@Aggregation`)

The same `@View`-on-base-table pattern applies to Ebean's `@Sum`/`@Aggregation`
group-by formulas — the Blaze-Persistence parallel is an `@EntityView` with
`@Mapping("SIZE(...)")`/`@Mapping("SUM(...)")` correlated mappings:

```java
@Entity
@View(name = "contact")
public class ContactStats {
  @Id
  private Long id;              // required so @Aggregation("count(id)") has something to
                                 // count; deliberately never selected/mapped - selecting it
                                 // would defeat the aggregation (one row per contact
                                 // instead of one row per customer)
  @ManyToOne
  private Customer customer;

  @Aggregation("count(id)")
  private Long contactCount;

  @Sum
  private Integer engagementScore;

  // getters ...
}
```

```java
public class ContactStatsDto {
  @DtoRef
  private final Long customerId;   // also the implicit GROUP BY key
  private final Long contactCount;
  private final Integer engagementScore;
  // constructor / getters ...
}
```

Because `customerId` uses `@DtoRef`, the generated fetch spec is
`select("customer,contactCount,engagementScore")` with **no join** — the query groups
by the FK column directly:

```sql
select t0.customer_id, count(t0.id), sum(t0.engagement_score)
from contact t0
group by t0.customer_id
```

---

## Performance notes

### Fail-fast, no accidental lazy loading

`mapTo(...)` forces `query.setUnmodifiable(true)` under the hood. If the mapper ever
needs a property that wasn't fetched, it throws `LazyInitialisationException`
immediately rather than silently issuing an extra query per row or returning `null`.
`InterceptReadOnly` (the unmodifiable-graph bean state) is also cheap — a `boolean[]
loaded` flag array plus a `frozen` flag, not a full second copy of bean state.

### One mapper instance per pair

Generated mappers are constructed once (in dependency order — a mapper with nested
mappers takes them as constructor params) and reused across every `mapTo(...)` call for
that pair, resolved and cached by `DtoMapperManager` keyed on `(sourceType, dtoType)`.

### `DtoMapContext` overhead only where it earns its keep

As shown above, the generator only involves `DtoMapContext` for mappers that can
actually be reached via more than one path in some graph (dedup) or that have nested
mappers of their own (need to thread the context down); a DTO that's only ever a
top-level query result skips it entirely.

### Fetch strategy and pagination carry over unchanged

Existing fetch-strategy control (`+query`/`+lazy`, `fetchQuery()`) and pagination
(including keyset pagination and `findPagedList()`) work the same whether the query
target is an entity graph or a `mapTo(...)` DTO graph — no special-casing needed.

---

## Which should I use?

- **`mapTo(Dto.class)`** — the target is a **nested** shape (has its own ToOne/ToMany
  DTO fields) that should mirror part of the entity graph; you want the fetch spec
  derived automatically and verified to match the DTO's declared shape.
- **`asDto(Dto.class)`** / `DB.findDto(...)` — the target is a **flat** row (report,
  summary, native/vendor SQL); you're comfortable with runtime-checked column-to-bean
  matching, or the SQL doesn't map cleanly to entity property paths at all.
- **Plain entity query** — the caller needs a real, persistable, mutable entity — not a
  read-only projection.

---

## Reference

### Why `package-info.java`?

`@DtoMapping` is declared on a package (`ElementType.PACKAGE`), not the DTO or the
entity, because:
- the DTO type is often owned/generated elsewhere (e.g. from an OpenAPI spec) and
  shouldn't need to be annotated with an internal persistence/entity type;
- one entity may be the source for several different DTOs (e.g. a summary vs. a detail
  view), and the same entity/DTO pair may need registering from multiple consuming
  modules.

### Annotations at a glance

| Annotation | Target | Purpose |
|---|---|---|
| `@DtoMapping(source=, target=)` | `package-info.java` | Registers an entity → DTO pair, triggers mapper generation |
| `@DtoMapping(..., builder=)` | `package-info.java` | `AUTO` (default, threshold-based) / `ALWAYS` / `NEVER` - builder-chain vs positional constructor |
| `@DtoMapping(..., name=, exclude=)` | `package-info.java` | Registers a named variant sharing the base mapping's generated class, excluding nested paths |
| `@DtoPath("a.b.c")` | DTO field/getter | Renamed and/or flattened multi-hop property mapping |
| `@DtoRef` | DTO field/getter | Id-only back-reference; breaks a cycle; root-selects the FK (no join) |
| `@DtoConvert(value=, method=)` | DTO field/getter | Custom scalar conversion - static (no registration) or instance (via `DtoConverterManager`) dispatch |
| `@DtoMixin(Target.class)` | Companion interface/class | Overlays `@DtoPath`/`@DtoRef`/`@DtoConvert` onto a DTO that can't be annotated directly |

### Parallels with other tools

If you're coming from another mapping library, here's the rough correspondence:

| Ebean | MapStruct | Blaze-Persistence |
|---|---|---|
| Generated `DtoMapper` per (source, DTO) pair | Generated `@Mapper` implementation | `@EntityView` (interface + runtime proxy) |
| `@DtoPath("a.b.c")` | `@Mapping(target = "x", source = "a.b.c")` | `@Mapping("a.b.c")` |
| `@DtoRef` | `@Context`/manual cycle-breaking (no dedicated annotation) | Sub-view referencing an id-only projection |
| `@DtoConvert(value=, method=)` | `@Mapping(qualifiedByName = "...")` / custom mapper methods | Custom converter/`@Mapping` expression |
| `@DtoMixin(Target.class)` | N/A (annotate the `@Mapper` interface's abstract methods instead) | N/A |
| `DtoMapContext` identity de-dup | Not built in (opt-in `@MappingTarget`/manual caching) | Built in (entity-view identity) |
| `@Entity @View` + `@Formula2`/`@Sum`/`@Aggregation` for computed DTO values | N/A (MapStruct doesn't touch SQL) | `@Mapping("SIZE(...)")` / `@Mapping("SUM(...)")` correlated mappings |

See [dto-mapping-design.md](../dto-mapping-design.md) for the full design rationale and
[dto-mapping-requirements.md](../dto-mapping-requirements.md) for the accepted/rejected
requirements this feature was scoped against (issue
[#2540](https://github.com/ebean-orm/ebean/issues/2540)).
