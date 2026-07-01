# Immutable Bean Cache — notes on multi-level / remote caching

These notes capture design thoughts for a possible future multi-level immutable bean cache,
where immutable beans may be cached remotely (for example Redis or a Postgres cache table)
in addition to an in-JVM cache.

## Current important constraint

`AssocOneHelp.read()` now uses `ImmutableBeanCache.getIfPresent(id)` as a direct-hit fast path.

That means:

- `getIfPresent(id)` is on the **row read hot path**
- it must remain **cheap and local**
- it should **not** perform network I/O
- it should **not** deserialize remote payloads
- it should **not** trigger loading or record misses

## Strong recommendation

For any multi-level cache design:

- **L1 cache** = in-JVM cache of already materialized immutable beans
- **L2 cache** = remote/shared cache of serialized immutable snapshots
- **Loader** = Ebean query using the configured fetch group

With that split:

- `getIfPresent(id)` => **L1 only**
- `getAll(ids)` => batch through **L1 -> L2 -> loader**

This preserves the `AssocOneHelp` fast path.

---

## Snapshot mindset

Remote cache entries should be treated as **immutable snapshots**, not just arbitrary beans.

A cached value is specific to:

- bean type
- bean id
- tenant (if multi-tenant)
- fetch-group / cache identity
- serializer/schema version

This matters because a `Customer` cached with:

- `select("name,version")`

is not equivalent to a `Customer` cached with:

- `select("name,version").fetch("billingAddress", "line1,city")`

## Key design recommendation

Remote keys should include at least:

- bean type
- bean id
- tenant id (if applicable)
- cache/fetch-group identity
- optionally serializer/schema version

Example shape:

- `immutable:Customer:basic:42`
- `immutable:Customer:withAddresses:42`

---

## Recommended multi-level flow

### L1

Store actual read-only `EntityBean` instances.

Responsibilities:

- support `getIfPresent(id)`
- avoid repeated deserialize cost
- avoid network calls on row read path

### L2

Store serialized immutable snapshots.

Responsibilities:

- batch lookup only
- support cross-JVM sharing
- feed L1 with materialized immutable beans

### Loader

Use the existing query/fetch-group-based loader for misses.

### Suggested `getAll(ids)` flow

1. Check L1
2. Batch remaining ids to L2
3. Deserialize L2 hits into read-only beans
4. Put those beans into L1
5. Batch remaining misses to DB loader
6. Freeze / ensure read-only beans
7. Write through to L2
8. Put into L1
9. Negative-cache true misses if desired

---

## Invalidation is more important than serialization

Things to think about:

- update/delete invalidation across JVMs
- local L1 invalidation when L2 entry is removed
- ordering relative to DB commit
- multiple cache instances for the same bean type but different fetch groups
- tenant-scoped invalidation

Recommended direction:

- keep current immutable-cache invalidation semantics
- add a remote invalidation/event mechanism for L2-backed caches
- each JVM should evict affected L1 entries when notified

Examples:

- Redis: pub/sub or streams
- Postgres cache table: NOTIFY/listen, polling, or invalidation table/outbox pattern

---

## Serialization format considerations

## JSON

### Pros

- human readable / debuggable
- easier rolling upgrades
- field-name based, so generally more tolerant of schema evolution
- good fit for Redis strings or Postgres JSONB
- easier operational debugging

### Cons

- larger payloads
- more CPU to serialize/deserialize
- nested graphs / enums / dates / inheritance need disciplined handling

## Kryo / generic binary serialization

### Pros

- smaller payloads
- often faster than JSON
- can preserve object graphs efficiently

### Cons

- more fragile across versions and rolling deploys
- class registration / compatibility pain
- harder to inspect/debug
- tighter coupling to JVM/class layout
- riskier for long-lived shared cache entries

## Recommendation

For a first remote/shared implementation:

- prefer **JSON** or another self-describing structured format
- if a binary format is later needed, prefer a stable schema-based format over generic object-graph serialization
- **do not start with Kryo** unless short-lived entries and tight deployment coordination are acceptable

---

## What to serialize

Avoid thinking in terms of serializing arbitrary live entity bean graphs directly.

A cleaner model is:

- serialize a **snapshot representation**
- deserialize into a fresh entity bean
- mark loaded properties appropriately
- freeze / ensure read-only state
- store the resulting materialized bean in L1

This gives more control over:

- loaded-property semantics
- read-only state
- subtype handling
- schema/version evolution

## Practical recommendation

Remote cache entries should represent exactly the configured fetch-group snapshot.

That means:

- cache what the fetch group loaded
- include nested associations loaded by that fetch group
- treat it as a self-contained immutable snapshot

This is simpler than trying to normalize the graph into many remote cache fragments and re-link it later.

---

## Redis vs Postgres cache table

## Redis

### Good for

- low latency
- batch lookup via MGET / pipelining
- TTL/eviction support
- natural shared-cache use case

### Tradeoffs

- extra infrastructure
- memory cost
- invalidation/event coordination still required

## Postgres cache table (including unlogged-style approach)

### Good for

- simpler ops if Postgres is already present
- easy batch lookup with `IN (...)`
- fewer moving parts than introducing Redis

### Tradeoffs

- slower than Redis for hot shared-cache usage
- adds pressure to Postgres
- TTL/cleanup becomes application responsibility
- still network/database I/O, so should remain off the `getIfPresent()` hot path

## Recommendation

- if the goal is a serious shared L2 cache, Redis is the more natural fit
- if the goal is pragmatic shared caching with minimal extra infrastructure, Postgres can work but should still be treated as L2-only

---

## Versioning / evolution

Whatever serializer is used, include versioning information.

Useful dimensions:

- serializer/schema version
- cache implementation version
- fetch-group/cache identity version

This helps when:

- fields are added/removed
- graph shape changes
- fetch-group definitions evolve

---

## Compression

If remote snapshots become large:

- compress only above a size threshold
- avoid compressing tiny payloads

This is especially relevant for JSON in Redis or Postgres L2.

---

## Observability

A multi-level cache should expose at least:

- L1 hit rate
- L2 hit rate
- DB loader rate
- deserialize failures
- invalidation counts
- average payload size
- cold-start amplification

Without this, it will be hard to judge whether the remote cache is helping.

---

## Overall recommended architecture

### Recommended model

- **L1**: actual read-only `EntityBean` instances
- **L2**: serialized immutable snapshots
- **Loader**: fetch-group-based DB query

### Method responsibilities

- `getIfPresent(id)` => **L1 only**
- `getAll(ids)` => **L1 + L2 + DB loader** in batches

This aligns well with the current `AssocOneHelp` optimization and keeps the row-read path fast.

---

## Bottom line

If/when multi-level immutable caching is explored, the main points to preserve are:

1. keep `getIfPresent()` local-only
2. do remote work only in batched `getAll()`
3. key by type + id + tenant + fetch-group/cache identity
4. treat remote values as immutable snapshots
5. prefer JSON/self-describing format first
6. be cautious with generic binary serializers like Kryo

---

## Possible follow-up

If this becomes active design work later, consider promoting these notes into one of:

- a dedicated design note under `docs/notes/`
- a GitHub issue / discussion for design iteration
- a lightweight ADR if this becomes a committed architectural direction
