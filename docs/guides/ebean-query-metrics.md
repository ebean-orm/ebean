# Guide: Ebean query metrics and naming

## Purpose

This guide explains the metrics Ebean captures, how the metric **name** for a query
is derived, and how you influence that name with `setLabel(..)` and **profile
locations**. It also covers secondary (lazy / query) load naming, the inline SQL
comment, collecting metrics at runtime, and how the names map to avaje-metrics tags.

Use this guide when you want to identify a query in metrics/telemetry, when a query
shows up under an unexpected metric name, or when wiring Ebean metrics into a reporter.

---

## Overview

Ebean records timing and counter metrics for the work it does. Every metric has a
**name** whose leading segment identifies the kind of work:

| Prefix | What it measures | Example name |
|---|---|---|
| `orm.` | Entity (ORM) query | `orm.Customer.findList`, `orm.CustomerFinder.byName` |
| `dto.` | DTO query | `dto.CustomerDto.byEmail` |
| `sql.query.` | Raw SQL query | `sql.query.<label>` |
| `sql.update.` / `sql.call.` | Raw SQL update / stored procedure call | `sql.update.<label>` |
| `orm.update.` | ORM update statement | `orm.update.<label>` |
| `iud.` | Bean insert / update / delete | `iud.Customer.insert` |
| `txn.main` / `txn.readonly` / `txn.named.` | Transactions | `txn.main`, `txn.named.processOrders` |
| `l2n.` | L2 cache region | `l2n.customer.hit` |

The rest of this guide focuses on **`orm.` query names**, which is where labels and
profile locations apply.

---

## How an ORM query name is derived

An entity query name has the form `orm.<identifier>`. The `<identifier>` comes from one
of three sources, in priority order:

1. **An explicit `setLabel(..)`** — prefixed with the bean type for disambiguation.
2. **A profile location** — used as-is (it is already a unique `Class.method` identifier).
3. **Neither** — the bean type plus the query type (e.g. `findList`).

| Root query source | Resulting name |
|---|---|
| `setLabel("custMain")` on `Customer` | `orm.Customer.custMain` |
| Profile location `CustomerFinder.byName` | `orm.CustomerFinder.byName` |
| Unlabelled `DB.find(Customer.class).findList()` | `orm.Customer.findList` |

The asymmetry is intentional: an explicit label is a short, ambiguous token (`custMain`
could be used for any bean), so the bean type is prefixed. A profile location is already
unique and type-independent, so it is used as-is.

### Step 1 - Label a query explicitly

```java
List<Customer> customers = DB.find(Customer.class)
  .setLabel("custMain")
  .findList();
// metric name: orm.Customer.custMain
```

DTO queries support `setLabel(..)` too, and follow the **same naming convention** as
ORM queries — an explicit label is prefixed with the DTO type, a profile location is
used as-is, and an unlabelled DTO query uses just the DTO type:

```java
DB.findDto(CustomerDto.class, sql)
  .setLabel("byEmail")
  .findList();
// metric name: dto.CustomerDto.byEmail
//   profile location only ->  dto.<location>      (no type prefix)
//   unlabelled            ->  dto.CustomerDto
```

### Step 2 - Use a profile location (preferred for finders / query beans)

A profile location identifies a query by its **call site** (`Class.method`) instead of a
hand-written label.

**The common case is automatic.** With Ebean's byte-code enhancement enabled (the normal
setup when using query beans / finders), Ebean assigns each query a profile location
derived from its call site — no code is required:

```java
List<Customer> customers = new QCustomer()
  .status.eq(Status.ACTIVE)
  .findList();
// metric name: orm.<CallingClass>.<method>  (often with a line number, see below)
```

The enhancer derives the location from the calling code (the method that runs the query),
and for many call sites it includes the **source line number** (e.g.
`CustomerService.find:42`), so distinct call sites — even in the same method — get distinct
names automatically.

**Setting one explicitly.** You can also set a profile location yourself, which is useful
without enhancement or to control the identity:

```java
ProfileLocation LOC = ProfileLocation.create();

List<Customer> customers = DB.find(Customer.class)
  .setProfileLocation(LOC)
  .where().eq("status", Status.ACTIVE)
  .findList();
// metric name: orm.<DeclaringClass>.<method>
```

Factory choices:

- `ProfileLocation.create()` — call site as `Class.method`, **no line number**.
- `ProfileLocation.createWithLine()` — includes the source line number
  (e.g. `CustomerService.find:42`), so two queries in the **same method** get
  **distinct** names.
- `ProfileLocation.create("label")` — a named location (used for named transactions).

> Note: a location with no line number (`create()`, or a call site the enhancer emits
> without a line) means two different queries in the same method share one name. The
> queried entity is still distinguishable downstream via the avaje-metrics `type` tag
> (see "Mapping to avaje-metrics tags" below). Use `createWithLine()` to separate
> same-method call sites in the name itself.

---

## Secondary (lazy / query) load naming

When a query lazy-loads or `fetchQuery()`-loads an association, Ebean issues a
**secondary** query. Its name **extends the parent query's full name** with the relative
path and the load mode (`lazy` or `query`), joined with `.`:

```
orm.<parent name without the "orm." prefix>.<path>.<loadMode>
```

So a secondary load is always an exact extension of its parent metric name, which makes
the relationship obvious in dashboards.

Example — root labelled `custMain` on `Customer`, chain `Customer -> orders -> details`:

Lazy loading:
```
orm.Customer.custMain
orm.Customer.custMain.orders.lazy
orm.Customer.custMain.orders.lazy.details.lazy
```

Secondary eager `fetchQuery()` loading:
```
orm.Customer.custMain
orm.Customer.custMain.orders.query
orm.Customer.custMain.orders.query.details.query
```

The same applies with a **profile-location** root (no explicit `setLabel`):
```
orm.CustomerFinder.byName
orm.CustomerFinder.byName.contacts.lazy
```

Unlike the root query, the secondary name is **not** bean-type prefixed by the loaded
type — it inherits the parent's name so it relates back to where the load originated.

---

## Inline SQL comment

When `includeLabelInSql` is enabled (the default), Ebean prepends the query's label (or
profile-location label) as an inline SQL comment, which is useful for matching slow
queries in database logs back to application code:

```sql
select /* CustomerFinder.byName */ t0.id, t0.name from be_customer t0 where ...
```

The comment uses the explicit `setLabel(..)` if present, otherwise the profile-location
label. Secondary queries use their full extended name
(e.g. `/* Customer.custMain.contacts.query */`). `EXISTS` / subquery forms are not
commented.

Disable it via the builder:

```java
Database.builder()
  .includeLabelInSql(false)
  .build();
```

---

## Collecting metrics at runtime

Read collected metrics through `Database.metaInfo()`:

```java
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.ServerMetrics;

ServerMetrics metrics = database.metaInfo().collectMetrics(); // resets counters

for (MetaQueryMetric q : metrics.queryMetrics()) {
  System.out.printf("%s type=%s count=%d total=%d mean=%d%n",
    q.name(),                 // e.g. orm.Customer.custMain
    q.type().getSimpleName(), // the queried bean/DTO type, e.g. Customer
    q.count(), q.total(), q.mean());
}
```

Key API:

- `database.metaInfo()` → `MetaInfoManager`.
- `collectMetrics()` collects and **resets**; `collectMetrics(false)` collects without
  reset; `visitMetrics(visitor)` for streaming.
- `ServerMetrics` exposes `queryMetrics()`, `timedMetrics()`, `countMetrics()`.
- `MetaQueryMetric` exposes `name()`, `label()`, `type()` (the queried `Class<?>`),
  `sql()`, `hash()`, plus timing `count()` / `total()` / `max()` / `mean()`.

---

## Mapping to avaje-metrics tags

When integrating with **avaje-metrics** (`avaje-metrics-ebean`
`DatabaseMetricSupplier`), the flat `orm.`/`dto.`/`sql.` names are translated to a tagged
form, with the bean type carried as a `type` tag:

```
ebean.query{kind=orm|dto|sql, type=<BeanSimpleName>, label=<rest of the name>}
```

Because the entity is available as the `type` tag, two different-entity queries that
share a profile-location name remain distinct series on tag-aware backends (OpenTelemetry,
Prometheus, StatsD) without needing the bean type in the name.

For the integration setup, see the avaje-metrics guide
[`add-ebean-metrics.md`](https://github.com/avaje/avaje-metrics/blob/master/docs/guides/add-ebean-metrics.md).

To capture the database execution plan (`EXPLAIN`) for slow queries identified by these
metrics, see [Ebean query plan capture](ebean-query-plan-capture.md).

---

## Troubleshooting

### A query shows up as `orm.<Bean>.findList` (no useful identity)

It has neither a label nor a profile location. Add `setLabel(..)` or a
`ProfileLocation`, or apply a profile location on the finder / query bean.

### Two queries in one method share a metric name

This happens when the profile location for those call sites has no line number. With
enhancement, many call sites already include a line number; for those that don't, use
`ProfileLocation.createWithLine()` to separate them by line, or give each an explicit
`setLabel(..)`. On tag-aware backends the avaje-metrics `type` tag already separates
different entity types.

### A secondary (lazy / query) load isn't grouped under its parent

Secondary names extend the parent's full name. If the parent has no label or profile
location, its name falls back to `orm.<Bean>.<queryType>` and the secondary extends
that. Give the root query a label or profile location for a stable parent name.
