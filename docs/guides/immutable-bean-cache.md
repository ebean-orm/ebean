# Immutable bean cache for read-only references

This guide shows how to use `ImmutableBeanCache` for read-mostly assoc-one
references (for example `Label` references reused across many entities).

Use this when you want:

- fewer lazy-load SQL calls for assoc-one references via caching
- reusable fetch-group-based loading for cache misses

---

## Step 1 - Build an immutable cache (typical builder use)

```java
FetchGroup<Label> fetchGroup = FetchGroup.of(Label.class)
  .select("version")
  .fetch("labelTexts", "locale, localeText")
  .build();

ImmutableBeanCache<Label> labelCache = ImmutableBeanCaches.builder(Label.class)
  .loading(database, fetchGroup)
  .maxSize(10_000)
  .maxIdleSeconds(300)
  .maxSecondsToLive(6_000)
  .build();
```

`loading(...)` uses the query shape:

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

## Use loading helper for simple memoization

If you don't need policy controls, use the shorthand helper:

```java
ImmutableBeanCache<Label> labelCache =
  ImmutableBeanCaches.loading(Label.class, database, FetchGroup.of(Label.class, "version"));
```

With `ebean-core` on the classpath, builder policy settings are backed by core
cache implementation (including periodic trim / eviction).

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

Use `ImmutableBeanCaches.builder(...)` when you need explicit TTL/max-size
policy. `ImmutableBeanCaches.loading(...)` remains the simple helper for
loader-based memoization.


---

## Testing checklist

1. Hit / partial hit / miss behavior for `getAll(ids)`
2. Unmodifiable path: no lazy SQL when reading loaded reference properties
3. Mutable path: additional unloaded properties can still lazy load
4. Secondary `fetchQuery` and `fetchLazy` paths inherit immutable caches
5. If needed associations are in fetch group, assert no extra SQL for those
   accesses
