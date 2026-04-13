# Guide: Write Ebean Queries with Query Beans

## Purpose

This guide gives step-by-step instructions for AI agents and developers to write
application queries using Ebean query beans.

Use this guide when the project already has Ebean configured and you need to:

- add a repository/service query
- replace string-based ORM queries with type-safe query beans
- tune what data is fetched to avoid over-fetching or N+1 issues
- return DTO projections for list screens or API responses

The default recommendation is:

1. Prefer query beans first
2. Prefer entity queries for domain logic
3. For read-only entity graphs, prefer `setUnmodifiable(true)`
4. Prefer DTO projection for summary/read-model use cases
5. Only drop to raw SQL when the ORM query cannot express the requirement cleanly

---

## Prerequisites

- The project already uses Ebean ORM
- Query bean generation is configured (for Maven this usually means
  `querybean-generator` is registered as an annotation processor)
- Entity beans already exist
- A compile/build has run successfully since the last entity model change

If query beans are not yet configured, first follow:
[`add-ebean-postgres-maven-pom.md`](add-ebean-postgres-maven-pom.md)

---

## Step 1 - Verify the generated `Q*` query bean exists

For each entity bean, Ebean generates a query bean with the same name prefixed
with `Q`.

Examples:

- `Customer` -> `QCustomer`
- `Order` -> `QOrder`
- `Contact` -> `QContact`

Import the generated type from the query bean package:

```java
import org.example.domain.query.QCustomer;
```

If the `Q*` type does not exist or the IDE cannot resolve it:

1. Confirm the entity compiled successfully
2. Run a normal project compile/build
3. If the entity was renamed or moved, run a full rebuild rather than relying on
   incremental compilation

### Important caveat - entity rename

After refactoring an entity name, old generated query beans can remain on disk
until the next full build. If both old and new `Q*` types appear to exist, do a
clean rebuild before editing application queries.

---

## Step 2 - Choose the terminal query method before writing predicates

Decide what the caller actually needs. This determines the terminal method and
often the right query shape.

| Need | Preferred method | Notes |
|------|------------------|-------|
| Check if at least one row exists | `exists()` | Cheapest choice for boolean existence checks |
| Load exactly one row by ID or unique key | `findOne()` | Only use when the predicate is truly unique |
| Load a list of entity beans | `findList()` | Default for list screens and domain logic |
| Count matching rows | `findCount()` | Prefer over loading entities just to count |
| Load a page plus optional total row count | `findPagedList()` | Use when the caller needs pagination metadata |
| Return DTO/read-model rows | `asDto(...).findList()` | Prefer this over partially loaded entities for API/view models |

### Example - existence check

```java
boolean alreadyUsed = new QCustomer()
  .email.equalTo(email)
  .exists();
```

### Example - unique lookup

```java
Customer customer = new QCustomer()
  .email.equalTo(email)
  .findOne();
```

Do **not** use `findOne()` for predicates that can match multiple rows.

---

## Step 3 - Build predicates by traversing properties and associations

With query beans, write predicates directly against properties. When you
traverse an association, Ebean adds the necessary joins automatically.

### Example - root property predicates

```java
List<Customer> customers = new QCustomer()
  .status.equalTo(Customer.Status.ACTIVE)
  .name.istartsWith("rob")
  .findList();
```

### Example - association traversal

```java
List<Customer> customers = new QCustomer()
  .billingAddress.city.equalTo("Auckland")
  .findList();
```

### Example - collection predicate

```java
List<Customer> customers = new QCustomer()
  .contacts.isEmpty()
  .findList();
```

### Agent rule

When adding a new query:

1. Start from the root entity that the caller wants back
2. Add predicates with query bean properties
3. Traverse relationships instead of writing manual join SQL
4. Keep property references type-safe; avoid string property names unless the API
   specifically requires them

---

## Step 4 - Add ordering, limits, and pagination deliberately

Do not leave list queries unordered unless the call site truly does not care.
For UI lists, APIs, and background jobs, explicit ordering is usually better.

### Example - ordered list with limit

```java
List<Customer> customers = new QCustomer()
  .status.equalTo(Customer.Status.ACTIVE)
  .orderBy().name.asc()
  .setMaxRows(50)
  .findList();
```

### Example - offset/limit pagination

```java
List<Customer> customers = new QCustomer()
  .status.equalTo(Customer.Status.ACTIVE)
  .orderBy().id.asc()
  .setFirstRow(offset)
  .setMaxRows(pageSize)
  .findList();
```

### Example - paged list with total count

```java
PagedList<Customer> page = new QCustomer()
  .status.equalTo(Customer.Status.ACTIVE)
  .orderBy().id.asc()
  .setFirstRow(offset)
  .setMaxRows(pageSize)
  .findPagedList();

page.loadRowCount();
List<Customer> customers = page.getList();
int totalRowCount = page.getTotalRowCount();
```

### Agent rule

- Use `findList()` when the caller only needs rows
- Use `findPagedList()` when the caller also needs page metadata or total counts
- Pair pagination with a stable `orderBy()` so page boundaries stay predictable

---

## Step 5 - Control fetched data with `select()` and `fetch()`

By default, entity queries can load more of the object graph than the caller
actually needs. Use `select()` and `fetch()` to control the root and association
properties that are loaded.

### Root properties with `select()`

Use `select()` to define which properties should be fetched on the root entity.

### Associated bean properties with `fetch()`

Use `fetch()` to define what should be fetched on associated paths.

### Example - partial entity query

```java
private static final QCustomer CUST = QCustomer.alias();
private static final QContact CONT = QContact.alias();

List<Customer> customers = new QCustomer()
  .select(CUST.name, CUST.status, CUST.whenCreated)
  .contacts.fetch(CONT.email)
  .name.istartsWith("rob")
  .findList();
```

In this example:

- `select(...)` tunes the root `Customer` properties
- `contacts.fetch(...)` tunes the associated `Contact` properties
- the query still returns `Customer` entity beans

### Agent rules for partial entity queries

1. Only use `select()`/`fetch()` when you know what the caller will read next
2. Do not treat partially loaded entities like fully populated API DTOs
3. If the caller only needs summary fields, prefer a DTO projection instead

---

## Step 6 - Use `setUnmodifiable(true)` for read-only entity graphs

`setUnmodifiable(true)` turns the returned object graph into an unmodifiable,
read-only graph.

This means:

- setters cannot mutate returned beans
- associated collections are unmodifiable
- lazy loading is disabled
- accessing an unloaded property throws `LazyInitialisationException`
- the query uses `PersistenceContextScope.QUERY`

### Example - read-only entity graph

```java
private static final QCustomer CUST = QCustomer.alias();
private static final QContact CONT = QContact.alias();

List<Customer> customers = new QCustomer()
  .select(CUST.name, CUST.status, CUST.whenCreated)
  .contacts.fetch(CONT.email)
  .status.equalTo(Customer.Status.ACTIVE)
  .setUnmodifiable(true)
  .findList();
```

### When to prefer `setUnmodifiable(true)`

Use it when the result is meant to be read-only, such as:

- service/query methods returning entity graphs for display or serialization
- query results you want the application to treat as immutable
- cached query results or other shared read models backed by entity graphs
- partial entity graphs where you want accidental lazy loading to fail fast

### When **not** to use it

Do **not** use `setUnmodifiable(true)` when the caller will:

- modify the beans and save them later
- rely on lazy loading of associations or unloaded scalar properties
- treat the result as a working persistence model rather than a read-only view

### Agent rule

If you are returning entity beans for read-only use, `setUnmodifiable(true)`
should be the default recommendation. If the caller needs a mutable model or a
serialized summary shape, choose mutable entities or DTO projection instead.

---

## Step 7 - Use `fetchQuery()` for to-many paths and `FetchGroup` for reusable query shapes

Ebean applies important SQL rules when translating ORM queries:

1. It does not generate SQL cartesian products
2. It honors `maxRows` in SQL

This means to-many paths often need special handling.

### Use `fetchQuery()` when:

- the query includes a `OneToMany` or `ManyToMany` path
- the query includes `setMaxRows(...)`
- the query loads multiple to-many paths
- you want the query shape to make the secondary-query behavior explicit

### Example - explicit secondary queries for to-many paths

```java
private static final QCustomer CUST = QCustomer.alias();

List<Order> orders = new QOrder()
  .customer.fetch(CUST.name)
  .lines.fetchQuery()
  .shipments.fetchQuery()
  .status.equalTo(Order.Status.NEW)
  .setMaxRows(100)
  .findList();
```

### Use `FetchGroup` when:

- the same fetch shape is reused in multiple places
- you want to separate predicate logic from fetch-shape tuning
- you want an immutable, static query-shape definition

### Example - reusable fetch group

```java
private static final QCustomer CUST = QCustomer.alias();

private static final FetchGroup<Customer> CUSTOMER_SUMMARY =
  QCustomer.forFetchGroup()
    .select(CUST.name, CUST.status, CUST.whenCreated)
    .billingAddress.fetch()
    .buildFetchGroup();

List<Customer> customers = new QCustomer()
  .select(CUSTOMER_SUMMARY)
  .status.equalTo(Customer.Status.ACTIVE)
  .findList();
```

### Agent rule

If the caller needs multiple to-many paths or a paged query, be suspicious of a
plain `fetch(...)` on those paths. `fetchQuery()` is often the safer default.

---

## Step 8 - Use DTO projection when the caller does not need entity beans

For list screens, API summaries, exports, or read-model views, the caller often
does **not** need managed entity beans. In those cases, project directly to a
DTO using `asDto(...)`.

### Example - DTO projection with query beans

```java
import static org.example.domain.query.QCustomer.Alias.id;
import static org.example.domain.query.QCustomer.Alias.name;

public record CustomerSummary(long id, String name) {}

List<CustomerSummary> summaries = new QCustomer()
  .select(id, name)
  .status.equalTo(Customer.Status.ACTIVE)
  .orderBy().name.asc()
  .asDto(CustomerSummary.class)
  .findList();
```

### Prefer DTO projection when:

- the caller will serialize the result directly
- only a subset of fields is needed
- the result is not going to be updated and saved back as an entity
- the query contains formulas or aggregation intended for a read model

---

## Step 9 - Only fall back to raw SQL when the ORM query is not a good fit

Prefer the following order:

1. Query bean query
2. Query bean query + `asDto(...)`
3. `DB.findDto(...)` or DTO query
4. Native SQL / `SqlQuery` / `RawSql`

### Typical reasons to use raw SQL

- vendor-specific SQL that query beans do not express well
- advanced aggregation or database functions
- hand-tuned reporting queries
- stored procedures or raw JDBC workflows

Do **not** jump to raw SQL just because the query joins multiple tables. Query
beans already handle ordinary relationship traversal well.

---

## Common anti-patterns

### Anti-pattern 1 - Using raw SQL first

**Avoid:**

```java
List<Customer> customers = DB.findNative(Customer.class,
  "select c.* from customer c join address a on a.id = c.billing_address_id where a.city = ?")
  .setParameter(1, city)
  .findList();
```

**Prefer:**

```java
List<Customer> customers = new QCustomer()
  .billingAddress.city.equalTo(city)
  .findList();
```

### Anti-pattern 2 - Using `findOne()` on a non-unique predicate

**Avoid:**

```java
Customer customer = new QCustomer()
  .status.equalTo(Customer.Status.ACTIVE)
  .findOne();
```

**Why:** Many rows can match; this is not a unique lookup.

### Anti-pattern 3 - Returning partially loaded entities as API models

If the caller only needs summary fields, return a DTO instead of partially
loaded entities that might later trigger more loading or confuse serializers.

### Anti-pattern 4 - Returning mutable entity graphs for read-only use

If the caller is only meant to read the result, prefer `setUnmodifiable(true)`
so accidental setter calls, collection mutation, and lazy loading fail fast.

### Anti-pattern 5 - Fetching every relationship "just in case"

Do not eagerly fetch large object graphs unless the immediate caller will use
them. Query tuning is part of the job.


---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Cannot resolve symbol QCustomer` | Query bean generation not configured or build not run | Check the annotation processor and run a build |
| Old `Q*` class still appears after entity rename | Stale generated source/class output | Run a clean rebuild |
| `findOne()` fails because multiple rows match | Predicate is not unique | Use `findList()` or tighten the predicate |
| Returned entities only have some fields loaded | `select()` or `FetchGroup` limited the query shape | Add the required fields or switch to DTO projection |
| Setter calls or collection mutation fail on query results | `setUnmodifiable(true)` returned a read-only graph | Remove `setUnmodifiable(true)` or treat the result as read-only |
| Accessing an unloaded property throws `LazyInitialisationException` | `setUnmodifiable(true)` disables lazy loading | Fetch the property up front or use DTO projection |
| Ebean executes secondary queries for a to-many path | ORM rules avoided cartesian product or honored `maxRows` | This is expected; use `fetchQuery()` explicitly when appropriate |

---

## Summary workflow for AI agents

When asked to add or modify an Ebean query:

1. Verify the relevant `Q*` type exists
2. Choose the terminal method first (`exists`, `findOne`, `findList`, `findPagedList`, `asDto`)
3. Add predicates with query bean properties and association traversal
4. Add explicit ordering and pagination if relevant
5. If the result is read-only entity data, consider `setUnmodifiable(true)`
6. Tune the fetch shape with `select()` / `fetch()` / `fetchQuery()` / `FetchGroup`
7. Prefer DTO projection for read models and serialized responses
8. Only use raw SQL if the ORM query is genuinely the wrong tool

---

## Related documentation

- [Add Ebean Postgres Maven POM](add-ebean-postgres-maven-pom.md)
- [Entity Bean Creation](entity-bean-creation.md)
- [Ebean query docs](https://ebean.io/docs/query/)
