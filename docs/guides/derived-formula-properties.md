# Guide: Derived / formula properties — `@Formula` and `@Formula2`

## Purpose

A *formula property* is a read-only entity property whose value is computed by a SQL
expression at query time rather than stored in its own column. Ebean has two
annotations for this:

- **`@Formula`** — you write the **physical SQL** for the `select` (and any `join`),
  using the `${ta}` placeholder for the base table alias. Maximum control; verbose.
- **`@Formula2`** — you write a **logical expression** using dot-notation property
  paths (e.g. `parent.familyName`). Ebean translates the paths to the correct table
  aliases and **adds the required JOINs automatically**.

`@Formula2` is intended as the easier, path-based replacement for `@Formula`. Both
produce read-only properties and behave the same way with respect to default
inclusion (see [Default inclusion](#default-inclusion-and-transient)).

---

## Quick comparison

| | `@Formula` | `@Formula2` |
|---|---|---|
| Expression | Physical SQL columns + aliases | Logical property paths |
| Table alias | `${ta}` placeholder you write | Resolved automatically |
| Joins | You write the `join` clause | Added automatically from the paths |
| Read only | ✅ | ✅ |
| Included by default | ✅ (use `@Transient` to opt out) | ✅ (use `@Transient` to opt out) |
| Usable in `select` / `where` / `orderBy` / `having` | ✅ | ✅ |
| Creates a DB column (DDL) | ❌ | ❌ |

---

## `@Formula` — physical SQL

You supply the SQL `select` fragment, and an optional `join`. Use `${ta}` wherever you
need the base table alias of the entity.

```java
@Entity
public class ParentPerson {

  // aggregation via a derived join; ${ta} is the base table alias
  @Formula(select = "coalesce(f2.child_count, 0)",
           join = "left join (select parent_id, count(*) as child_count"
                + " from child group by parent_id) f2 on f2.parent_id = ${ta}.id")
  Integer childCount;

  // coalesce across a joined table using an explicit join alias (j1)
  @Formula(select = "coalesce(${ta}.family_name, j1.family_name)",
           join = "join parent_person j1 on j1.id = ${ta}.parent_id")
  String effectiveFamilyName;
}
```

Notes:
- The `join` string must start with `join` or `left join`.
- You manage the join aliases (`j1`, `f2`, …) yourself and reference them in `select`.
- `@Formula` is `@Repeatable` and supports a `platforms()` restriction.

---

## `@Formula2` — logical property paths

Write the expression using property paths. Ebean resolves each path to the right table
alias and adds the joins it needs.

```java
@Entity
public class ParentPerson {

  @ManyToOne
  GrandParentPerson parent;

  String familyName;

  // Ebean automatically left joins 'parent' and resolves the aliases
  @Formula2("coalesce(familyName, parent.familyName)")
  String derivedFamilyName;
}
```

A query selecting `derivedFamilyName` produces (roughly):

```sql
select t0.id, coalesce(t0.family_name, t1.family_name)
from parent_person t0
left join grand_parent_person t1 on t1.id = t0.parent_id
```

Multi-level paths join through each step:

```java
// joins parent and parent.parent automatically
@Formula2("coalesce(familyName, parent.familyName, parent.parent.familyName)")
String deepFamilyName;
```

`@Formula2` works wherever a normal property does — the required joins are added
automatically in each case:

```java
// selected explicitly
DB.find(ParentPerson.class).select("derivedFamilyName").findList();

// used in where (auto-joins even when not selected)
DB.find(ParentPerson.class).where().eq("derivedFamilyName", "Smith").findList();

// used in order by
DB.find(ParentPerson.class).orderBy("derivedFamilyName").findList();

// referenced via a path from another bean
DB.find(ChildPerson.class).where().eq("parent.derivedFamilyName", "Smith").findList();
```

It also resolves correctly inside nested `fetch()` joins, so a `@Formula2` on a fetched
association is computed with its own joins relative to that association.

Notes:
- The expression supports any SQL function whose arguments are logical property paths.
- `@Formula2` supports a `platforms()` restriction.
- No `${ta}` and no hand-written join — that is the point of `@Formula2`.

---

## Default inclusion and `@Transient`

Both annotations are **included in queries by default** (just like a normal mapped
property). When no explicit `select()`/`fetch()` is given, the formula — and for
`@Formula2` the joins it requires — are added to the query.

Add `@Transient` to make the formula **opt-in**: it is then **not** selected by default
and must be requested explicitly via `select()` or `fetch()`. Do this when the formula
(or the joins it needs) is relatively expensive.

```java
// not selected by default; must be requested explicitly
@Transient
@Formula2("coalesce(familyName, parent.familyName)")
String lazyDerivedFamilyName;
```

```java
DB.find(ParentPerson.class)
  .select("lazyDerivedFamilyName")   // explicitly included, join auto-added
  .findList();
```

This is the same `@Transient` opt-out mechanism used by `@Formula`.

---

## Which should I use?

- Prefer **`@Formula2`** for expressions over property paths (coalesce/case/functions
  across associations). It is shorter, refactor-friendly, and the joins stay correct as
  the model changes.
- Use **`@Formula`** when you need raw SQL that does not map cleanly to property paths —
  for example a derived aggregate sub-select / dynamic view, or vendor-specific SQL.

For read models that exist only to carry computed values, also consider projecting to a
DTO instead of mapping the formula onto the entity — see
[writing-ebean-query-beans.md](writing-ebean-query-beans.md).
