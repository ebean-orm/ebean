# Guide: Using `RawSql` with Ebean

## Purpose

`RawSql` lets you back an Ebean bean with a **hand-written SQL query** instead of
Ebean generating the SQL from the entity mapping. Ebean still handles object
mapping (result set columns → bean properties), lazy loading of associated beans,
and - depending on how the `RawSql` is built - dynamic `WHERE`/`HAVING` predicates
added through the normal query API.

Use this guide when you need to:

- run vendor-specific SQL, complex aggregation, or reporting queries that don't
  map cleanly to an ORM query
- reuse a hand-tuned query but still want typed/dynamic predicates, paging, or
  `ORDER BY` added by the caller
- back a query bean (`Q*`) or DTO-like bean with SQL containing a CTE, window
  function, or subquery in the `FROM` clause

Prefer ordinary query bean queries first - see
[Write Ebean queries with query beans](writing-ebean-query-beans.md), Step 9,
for the full decision order (query bean → `asDto()` → DTO query → raw SQL).
This guide covers raw SQL once you've decided it's the right tool.

---

## The bean behind a `RawSql` query

A bean queried with `RawSql` is not necessarily backed by a physical table. Annotate
it `@Entity @Sql` to tell Ebean it is mapped via `RawSql` rather than table DDL:

```java
@Entity
@Sql
public class OrderAggregate {

  @OneToOne
  Order order;

  Double totalAmount;
  Long totalItems;

  // getters/setters
}
```

`@Sql` beans still get a generated query bean (`QOrderAggregate`) if the
querybean-generator annotation processor is configured - see
[Using `RawSql` with query beans](#using-rawsql-with-query-beans) below.

You can also query an ordinary table-backed `@Entity` with `RawSql` - the column
mapping just needs to line up with that entity's properties.

---

## Building a `RawSql` - three factory methods

`RawSqlBuilder` has three ways to construct a `RawSql`, depending on how much of
the SQL Ebean needs to understand:

| Method | SELECT columns parsed? | Dynamic WHERE/HAVING/ORDER BY? | Use for |
|--------|------------------------|------------------------|---------|
| `RawSqlBuilder.parse(sql)` | Yes | Yes | Ordinary `SELECT ... FROM ... WHERE ...` statements |
| `RawSqlBuilder.unparsed(sql)` | No | No | Fixed SQL that never needs additional predicates |
| `RawSqlBuilder.withPlaceholders(sql)` | No (explicit `columnMapping()` required) | Yes, via `${where}` / `${andWhere}` / `${having}` / `${andHaving}` / `${orderBy}` / `${andOrderBy}` | CTEs, window functions, subqueries - SQL that keyword-based parsing can't handle |

### `parse(sql)` - the common case

`parse(sql)` scans the SQL text for the `select` / `from` / `where` / `group by`
/ `having` / `order by` keywords to work out the SELECT column list (so it can
validate your column mappings) and the injection points for dynamic `WHERE`/
`HAVING` expressions.

```java
RawSql rawSql = RawSqlBuilder.parse(
    "select c.id, c.name, c.status from customer c")
  .columnMapping("c.id", "id")
  .columnMapping("c.name", "name")
  .columnMapping("c.status", "status")
  .create();

List<Customer> customers = DB.find(Customer.class)
  .setRawSql(rawSql)
  .where().eq("status", Customer.Status.ACTIVE)
  .orderBy("name")
  .findList();
```

Because the SQL is parsed, mistakes in `columnMapping()` (unknown column, wrong
order for `unparsed`-style mappings) are caught early. **This fails on SQL the
keyword parser can't make sense of** - a `WITH` CTE, a window function, a
subquery in `FROM`, etc. - because the keyword positions found don't correspond
to the outer query's real structure. Use `withPlaceholders(sql)` for that SQL
instead (see below).

### `unparsed(sql)` - fixed queries

`unparsed(sql)` skips all parsing. The SQL is used exactly as written, and **no
further `WHERE`/`HAVING`/`ORDER BY` can be added** by the caller - useful for a
completely fixed reporting query with no caller-supplied filtering.

```java
RawSql rawSql = RawSqlBuilder.unparsed(
    "select id, name, status from customer where status = 'ACTIVE'")
  .columnMapping("id", "id")
  .columnMapping("name", "name")
  .columnMapping("status", "status")
  .create();

List<Customer> customers = DB.find(Customer.class)
  .setRawSql(rawSql)
  .findList();
```

Column mappings for `unparsed(sql)` must be supplied **in the same order** as
the columns appear in the SQL, since there's no parsing to match them by name.

### `withPlaceholders(sql)` - complex SQL (CTEs, window functions, subqueries)

`withPlaceholders(sql)` avoids keyword scanning entirely. You mark exactly where
a dynamic `WHERE`/`HAVING`/`ORDER BY` expression should be injected using
placeholder tokens, and column mappings are always explicit (as with `unparsed`).

#### Placeholder reference

| Placeholder | Meaning | Use when |
|-------------|---------|----------|
| `${where}` | Insert a new `WHERE <expr>` clause here | No static `WHERE` clause exists yet at this point in the SQL |
| `${andWhere}` | Insert `AND <expr>` here | A static `WHERE ...` clause already exists in the SQL and you want to append to it |
| `${having}` | Insert a new `HAVING <expr>` clause here | No static `HAVING` clause exists yet at this point in the SQL |
| `${andHaving}` | Insert `AND <expr>` here | A static `HAVING ...` clause already exists in the SQL and you want to append to it |
| `${orderBy}` | Insert a new `ORDER BY <expr>` clause here | No static `ORDER BY` clause exists yet at this point in the SQL, and callers may supply `.orderBy(...)` |
| `${andOrderBy}` | Insert `, <expr>` here | A static `ORDER BY ...` clause already exists in the SQL and you want callers to be able to append extra sort columns to it |

Rules:

- At least one placeholder is required - `withPlaceholders(sql)` throws
  `IllegalArgumentException` if none of the six tokens are present.
- Use only the placeholders you need. Omit `${where}`/`${andWhere}` entirely if
  the query never needs a dynamic `WHERE` (e.g. only a dynamic `HAVING` on an
  aggregate). Omit `${having}`/`${andHaving}` if there's no dynamic `HAVING`.
  Omit `${orderBy}`/`${andOrderBy}` if the ordering is always fixed.
- Explicit `columnMapping()` is required for every returned column - there is no
  column-list parsing to infer names from.
- **A caller-supplied `.orderBy(...)`/`.order(...)` is only applied if the SQL
  contains an `${orderBy}` or `${andOrderBy}` placeholder.** Without one of
  those placeholders there is no defined injection point for dynamic ordering,
  so any `.orderBy(...)` call on the query is safely ignored rather than risk
  producing invalid SQL - even if the template has a static trailing
  `ORDER BY ...` of its own. If you need callers to be able to influence
  ordering, add `${orderBy}` (no existing static order by) or `${andOrderBy}`
  (append after an existing static order by).
- Any other static SQL that follows a `${where}`/`${having}` placeholder (e.g.
  a trailing `GROUP BY`) is preserved and correctly positioned **after**
  whatever dynamic expression gets injected at that placeholder.

#### Example - CTE with `${where}`

```java
String sql = """
  with order_totals as (
    select o.id as order_id, sum(d.qty * d.unit_price) as total_amount
    from o_order o
    join o_order_detail d on d.order_id = o.id
    group by o.id
  )
  select order_id, total_amount
  from order_totals
  ${where}
  order by order_id
  """;

RawSql rawSql = RawSqlBuilder.withPlaceholders(sql)
  .columnMapping("order_id", "order.id")
  .columnMapping("total_amount", "totalAmount")
  .create();

List<OrderAggregate> list = DB.find(OrderAggregate.class)
  .setRawSql(rawSql)
  .where().gt("totalAmount", 100)
  .findList();
```

`total_amount` is a genuine column of the `order_totals` CTE here, so it's valid
to filter on it in the outer `WHERE` - this only works because the aggregate is
computed inside the CTE rather than as a same-level `SELECT` alias.

#### Example - static `WHERE` already present, append with `${andWhere}`

```java
String sql = "... from order_totals where total_amount > 0 ${andWhere} order by order_id";

RawSql rawSql = RawSqlBuilder.withPlaceholders(sql)
  .columnMapping("order_id", "order.id")
  .columnMapping("total_amount", "totalAmount")
  .create();

// executed SQL: ... where total_amount > 0 and total_amount > ? order by order_id
DB.find(OrderAggregate.class)
  .setRawSql(rawSql)
  .where().gt("totalAmount", 100)
  .findList();
```

#### Example - `${having}` only, filtering on an aggregate directly

No `WHERE` placeholder is needed if you only ever filter on the aggregate value:

```java
String sql =
  "select o.id as order_id, sum(d.qty * d.unit_price) as total_amount" +
  " from o_order o join o_order_detail d on d.order_id = o.id" +
  " group by o.id" +
  " ${having}" +
  " order by order_id";

RawSql rawSql = RawSqlBuilder.withPlaceholders(sql)
  .columnMapping("order_id", "order.id")
  .columnMapping("total_amount", "totalAmount")
  .create();

List<OrderAggregate> list = DB.find(OrderAggregate.class)
  .setRawSql(rawSql)
  .having().gt("totalAmount", 100)
  .findList();
```

The dynamic `HAVING` clause is injected before the static trailing `ORDER BY`,
even though `${having}` is the only placeholder present. Because there's no
`${orderBy}`/`${andOrderBy}` placeholder here, a caller-supplied `.orderBy(...)`
would be ignored - the ordering stays fixed as `order by order_id`.

#### Example - both `${where}` and `${having}`

```java
String sql =
  "select o.id as order_id, sum(d.qty * d.unit_price) as total_amount" +
  " from o_order o join o_order_detail d on d.order_id = o.id" +
  " ${where}" +
  " group by o.id" +
  " ${having}" +
  " order by order_id";

RawSql rawSql = RawSqlBuilder.withPlaceholders(sql)
  .columnMapping("order_id", "order.id")
  .columnMapping("total_amount", "totalAmount")
  .create();

List<OrderAggregate> list = DB.find(OrderAggregate.class)
  .setRawSql(rawSql)
  .where().gt("order.id", 0)
  .having().gt("totalAmount", 50)
  .findList();
```

Both the dynamic `WHERE` and dynamic `HAVING` are injected at their respective
placeholders, and the trailing `order by order_id` is preserved after the
`HAVING` clause.

#### Example - `${orderBy}`, fully dynamic ordering

Use `${orderBy}` when there's no static default ordering and you want the
caller's `.orderBy(...)` to control it entirely:

```java
String sql =
  "with order_totals as (" +
  "  select o.id as order_id, sum(d.qty * d.unit_price) as total_amount" +
  "  from o_order o join o_order_detail d on d.order_id = o.id" +
  "  group by o.id" +
  ")" +
  " select order_id, total_amount from order_totals" +
  " ${where}" +
  " ${orderBy}";

RawSql rawSql = RawSqlBuilder.withPlaceholders(sql)
  .columnMapping("order_id", "order.id")
  .columnMapping("total_amount", "totalAmount")
  .create();

// executed SQL: ... where total_amount > ? order by total_amount desc
List<OrderAggregate> list = DB.find(OrderAggregate.class)
  .setRawSql(rawSql)
  .where().gt("totalAmount", 0)
  .orderBy("totalAmount desc")
  .findList();
```

If the caller doesn't call `.orderBy(...)`, nothing is injected at `${orderBy}`
and no `ORDER BY` clause is emitted at all.

#### Example - `${andOrderBy}`, appending to a static default ordering

Use `${andOrderBy}` when there's a sensible static default ordering but you
want callers to be able to add extra tie-breaker sort columns:

```java
String sql =
  "... from order_totals" +
  " ${where}" +
  " order by total_amount desc ${andOrderBy}";

RawSql rawSql = RawSqlBuilder.withPlaceholders(sql)
  .columnMapping("order_id", "order.id")
  .columnMapping("total_amount", "totalAmount")
  .create();

// executed SQL: ... order by total_amount desc , order_id
List<OrderAggregate> list = DB.find(OrderAggregate.class)
  .setRawSql(rawSql)
  .where().gt("totalAmount", 0)
  .orderBy("order.id")
  .findList();
```

---

## Using `fetchQuery()` to build out more of the graph

A `RawSql` query can be the **root query** and still use `fetchQuery(path)` the
same way an ordinary ORM query does - Ebean runs the raw SQL for the root rows,
then runs additional secondary ORM queries to populate the requested paths. This
lets you hand-write only the part of the query that needs raw SQL (e.g. an
aggregate/CTE) and let the ORM build out the rest of the object graph normally.

```java
List<OrderAggregate> list = DB.find(OrderAggregate.class)
  .setRawSql(rawSql)           // root query - runs the CTE/aggregate SQL
  .fetchQuery("order")         // secondary query - loads the full Order
  .fetchQuery("order.details") // secondary query - loads Order.details
  .where().gt("totalAmount", 50)
  .findList();
```

This executes **three** queries: the raw SQL root query, then one secondary
query per `fetchQuery(path)` call.

**Important**: if the raw SQL's column mapping only populates part of an
association (e.g. only `order.id`, as in the examples above), that association
is a *partial reference*. To load a nested to-many under it (e.g.
`order.details`), you must add an explicit `fetchQuery(...)` (or `fetch(...)`)
for the **intermediate path** (`order`) as well as the nested path
(`order.details`) - `fetchQuery("order.details")` alone will leave `details` as
a deferred/lazy collection, because Ebean doesn't otherwise have a fetch node
for `order` to hang the secondary query off. If the raw SQL already selects the
full set of columns for an association directly (no partial reference), this
extra step isn't needed.

This is the same `fetchQuery()` mechanism used for ordinary query bean queries -
see [Use `fetchQuery()` for to-many paths](writing-ebean-query-beans.md#step-7---use-fetchquery-for-to-many-paths-and-fetchgroup-for-reusable-query-shapes)
for background on why to-many paths are loaded via secondary queries rather than
a single joined query.

---

## Column mapping

Every `RawSqlBuilder` (except a bare `unparsed(sql)` with implicit positional
mapping) uses `columnMapping(dbColumn, propertyName)` to map SQL result columns
to bean properties:

```java
.columnMapping("order_id", "order.id")   // maps to the "order" association's "id" property
.columnMapping("total_amount", "totalAmount")
```

- Dotted property paths (e.g. `"order.id"`) map a column into a nested/associated
  bean property.
- `columnMappingIgnore(dbColumn)` marks a selected column as intentionally unmapped
  (present in the SQL but not needed on the bean).
- `tableAliasMapping(tableAlias, path)` bulk-renames every mapping using a given
  SQL table alias to be prefixed with a bean property path - handy when a `parse()`
  query selects many columns from a joined table (e.g. alias `c` → path `customer`)
  and you don't want to repeat the prefix in every `columnMapping()` call.

---

## Using `RawSql` with query beans

`RawSql` is not limited to the plain `Query<T>` API - it also works with a
generated query bean, giving type-safe `where()`/`having()`-equivalent
expressions (as bean properties) over hand-written SQL. Every generated query
bean exposes `setRawSql(...)`:

```java
RawSql rawSql = RawSqlBuilder.parse("select id, name, status from customer")
  .columnMapping("id", "id")
  .columnMapping("name", "name")
  .columnMapping("status", "status")
  .create();

List<Customer> customers = new QCustomer()
  .setRawSql(rawSql)
  .status.equalTo(Customer.Status.ACTIVE) // typed expression, injected into the parsed WHERE clause
  .findList();
```

This also works with `withPlaceholders(sql)` and an `@Sql` query bean:

```java
List<OrderAggregate> list = new QOrderAggregate()
  .setRawSql(rawSql) // built with withPlaceholders() as shown above
  .totalAmount.gt(100)
  .findList();
```

The typed property expression (`.totalAmount.gt(100)`) is translated to a bound
predicate and injected at the `${where}`/`${having}` placeholder position, exactly
as `.where().gt("totalAmount", 100)` would be on the plain `Query<T>` API.

---

## Common anti-patterns

### Anti-pattern 1 - reaching for raw SQL before trying a query bean

Complex-looking joins are often just ordinary association traversal in a query
bean. Don't use raw SQL just because a query touches several tables - see
[Write Ebean queries with query beans](writing-ebean-query-beans.md).

### Anti-pattern 2 - using `parse(sql)` on a CTE or window-function query

`parse(sql)` will throw a parsing exception (or silently mis-locate the WHERE
injection point) on SQL it can't understand structurally. If your SQL starts
with `WITH ...` or has a subquery in `FROM`, use `withPlaceholders(sql)` instead.

### Anti-pattern 3 - filtering on a same-level SELECT alias

You cannot add a dynamic `WHERE` predicate on a `SELECT`-clause alias in the
same query level (e.g. `select sum(x) as total ... ${where}` - `total` isn't a
real column yet at the `WHERE` stage of that query level). Either:

- move the aggregation into a CTE and filter on the CTE's output column in the
  outer query (`WHERE` case), or
- use `${having}`/`${andHaving}` to filter on the aggregate at the `HAVING` stage
  of the same query level, where the aggregate expression is valid.

### Anti-pattern 4 - forgetting `columnMapping()` with `unparsed()`/`withPlaceholders()`

Both `unparsed(sql)` and `withPlaceholders(sql)` require **every** returned
column to be explicitly mapped (or explicitly ignored via
`columnMappingIgnore(...)`) - there's no column-list parsing to infer them.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `RuntimeException: Error parsing sql, can not find ... keyword` | `parse(sql)` used on SQL with a CTE, window function, or subquery in `FROM` | Use `RawSqlBuilder.withPlaceholders(sql)` instead |
| `IllegalArgumentException: withPlaceholders() requires at least one of ${where}, ${andWhere}, ${having}, ${andHaving}, ${orderBy}, ${andOrderBy}...` | None of the six placeholder tokens were found in the SQL | Add the appropriate placeholder token at the injection point |
| Dynamic `WHERE`/`HAVING` predicate silently has no effect, or query throws | Used `unparsed(sql)` and then tried to add a predicate | `unparsed(sql)` queries cannot be modified - switch to `parse(sql)` or `withPlaceholders(sql)` |
| Generated SQL is invalid / clauses appear in the wrong order | Predicates added via `.where()`/`.having()` don't match the placeholders actually present in the SQL | Make sure `${where}`/`${having}` (or the `and` variants) exist at the point you expect predicates to be injected |
| `.orderBy(...)`/`.order(...)` on the query silently has no effect | The SQL has no `${orderBy}`/`${andOrderBy}` placeholder | This is by design - without one of those placeholders there's no defined injection point, so the ordering is ignored rather than corrupting the SQL. Add `${orderBy}` or `${andOrderBy}` if you need caller-controlled ordering |
| `Unknown column` / unmapped property error | Missing `columnMapping()` for a selected column | Add a `columnMapping(...)` or `columnMappingIgnore(...)` for every SQL column |
| `fetchQuery("a.b")` collection stays deferred/lazy | `a` is a partial reference from the raw SQL column mapping (e.g. only `a.id` mapped), and there's no fetch node for `a` itself | Add `fetchQuery("a")` (or `fetch("a")`) alongside `fetchQuery("a.b")` |

---

## Related documentation

- [Write Ebean queries with query beans](writing-ebean-query-beans.md)
- [Derived / formula properties (`@Formula`, `@Formula2`)](derived-formula-properties.md)
- [Ebean query docs](https://ebean.io/docs/query/)
