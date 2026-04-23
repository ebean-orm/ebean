# Immutable bean cache for read-only references

This guide shows how to use `ImmutableBeanCache` for read-mostly assoc-one
references (for example `Label` references reused across many entities).

Use this when you want:

- fewer lazy-load SQL calls for assoc-one references via caching
- reusable fetch-group-based loading for cache misses

---

## Step 1 - Build a loader-backed immutable cache

Use `ImmutableBeanCaches.loading(...)` with a `FetchGroup` and unmodifiable
query semantics.

```java
// define a FetchGroup to control what the cached beans contain
FetchGroup<Label> fetchGroup = FetchGroup.of(Label.class, "version");

// build an ImmutableBeanCache
ImmutableBeanCache<Label> labelCache =
  ImmutableBeanCaches.loading(Label.class, DB.getDefault(), fetchGroup);
```

This helper uses the query shape:

- `select(fetchGroup)`
- `setUnmodifiable(true)`
- `where().idIn(ids)`
- `findMap()`

---

## Step 2 - Attach cache to the root query

```java
AttributeDescriptor one = DB.find(AttributeDescriptor.class)
  .setId(id)
  .setUnmodifiable(true)
  .using(labelCache)
  .findOne();
```

`using(...)` is on the root query. Ebean will use this cache for matching
bean types when resolving references.

---

## Unmodifiable vs mutable query behavior

### Unmodifiable query path

`setUnmodifiable(true)` disables lazy loading. If you need association content
in cached beans make sure that is included in the fetch group.

```java
FetchGroup<Label> withTexts = FetchGroup.of(Label.class)
  .select("version")
  .fetch("labelTexts", "locale, localeText")
  .build();
```

### Mutable query path

On a mutable query (no `setUnmodifiable(true)`), references populated from the
immutable cache are still mutable beans in that object graph. Additional
unloaded properties can still lazy load as normal.

Typical pattern:

1. cache serves already-loaded reference properties (for example `version`)
2. later access to unloaded properties (for example `labelTexts`) triggers
   normal lazy loading

---

## Understand secondary query behavior (`+query`, `+lazy`)

When root queries execute secondary loads (`fetchQuery(...)` or `fetchLazy(...)`),
the immutable caches configured on the root query are propagated to those
secondary queries.

That means assoc-one references resolved in secondary query paths can still hit
the immutable cache.

---

## Operational note (TTL / max size)

`ImmutableBeanCaches.loading(...)` is a memoizing helper. If you need explicit
TTL/max-size policy, provide a custom `ImmutableBeanCache` implementation with
your preferred cache backend/policy.


---

## Testing checklist

1. Hit / partial hit / miss behavior for `getAll(ids)`
2. Unmodifiable path: no lazy SQL when reading loaded reference properties
3. Mutable path: additional unloaded properties can still lazy load
4. Secondary `fetchQuery` and `fetchLazy` paths inherit immutable caches
5. If needed associations are in fetch group, assert no extra SQL for those
   accesses
