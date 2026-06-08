# Guide: Ebean query plan capture

## Purpose

This guide explains how to enable and configure **query plan capture** in Ebean — the
mechanism that captures the database's actual execution plan (via `EXPLAIN`) for slow
queries, so you can diagnose missing indexes and poor plans in production.

Use this guide when you want Ebean to record real query plans, when tuning the capture
thresholds and load limits, or when wiring a listener to ship captured plans somewhere.

---

## Overview

Query plan capture is a **two-phase** mechanism:

1. **Bind capture** — when enabled, Ebean watches query executions and, for queries
   slower than a threshold, captures the actual **bind values** that were used. This is
   cheap: it just remembers the parameters of a slow execution.
2. **Plan capture** — using those captured bind values, Ebean runs `EXPLAIN <sql>`
   against the database to obtain the execution plan, producing `MetaQueryPlan` results
   that are handed to a `QueryPlanListener`.

Plan capture is split this way so the expensive `EXPLAIN` work (actual database load)
happens periodically or on demand, against representative bind values, rather than on
every slow query.

Two ways to trigger phase 2:

- **Automatic periodic capture** — a background timer collects plans on a schedule.
- **On demand** — call the `MetaInfoManager` API to arm and collect plans yourself
  (this is what remote tooling such as ebean-insight uses).

Only ORM entity SELECT queries are plan capable. Specifically **excluded** are:

- **`DtoQuery`** (`dto.*` metrics) — DTO queries have no bind capture.
- **`SqlQuery`** / raw SQL (`sql.query.*`) — raw SQL queries have no bind capture.
- **Update / DML** — `orm.update.*`, `iud.*`, `sql.update.*`, `sql.call.*`.

Bind capture is wired only into the ORM query path (per-entity `BeanDescriptor`), and the
init/collect API iterates ORM entity descriptors only, so DTO and raw SQL queries — even
though they produce timing metrics — never capture bind values and cannot be `EXPLAIN`'d.

---

## Step 1 - Enable bind capture

Bind capture is the master switch; nothing is captured until it is on.

```java
Database database = Database.builder()
  .queryPlanEnable(true)               // turn on bind capture
  .queryPlanThresholdMicros(100_000)   // capture binds for queries slower than 100ms
  .build();
```

- `queryPlanEnable(boolean)` — enable bind capture. Default **false**.
- `queryPlanThresholdMicros(long)` — global execution-time threshold (microseconds) a
  query must exceed before its bind values are captured. Default **`Long.MAX_VALUE`**
  (effectively off), so you must either lower it or arm specific plans by hash (Step 3).

Equivalent `application.properties` (avaje-config / properties):

```properties
queryPlan.enable=true
queryPlan.thresholdMicros=100000
```

---

## Step 2 - Enable automatic periodic capture (optional)

To have Ebean periodically run `EXPLAIN` for armed queries and report the plans:

```java
Database database = Database.builder()
  .queryPlanEnable(true)
  .queryPlanThresholdMicros(100_000)
  .queryPlanCapture(true)                  // turn on the periodic capture timer
  .queryPlanCapturePeriodSecs(600)         // every 10 minutes (default)
  .queryPlanCaptureMaxTimeMillis(10_000)   // stop after 10s of capturing per cycle
  .queryPlanCaptureMaxCount(10)            // at most 10 plans per cycle
  .queryPlanListener(capture -> {
    for (var plan : capture.plans()) {
      System.out.println(plan.label() + "\n" + plan.plan());
    }
  })
  .build();
```

- `queryPlanCapture(boolean)` — enable the background periodic capture. Default **false**.
- `queryPlanCapturePeriodSecs(long)` — capture frequency in seconds. Default **600** (10 min).
- `queryPlanCaptureMaxTimeMillis(long)` — per-cycle time budget; capture stops once
  exceeded, bounding the database load. Default **10000** (10s).
- `queryPlanCaptureMaxCount(int)` — max plans captured per cycle. Default **10**.
- `queryPlanListener(QueryPlanListener)` — receives each `QueryPlanCapture`. If not set,
  the default listener logs plans to the `io.ebean.QUERYPLAN` logger at `INFO`.

Properties form:

```properties
queryPlan.enable=true
queryPlan.thresholdMicros=100000
queryPlan.capture=true
queryPlan.capturePeriodSecs=600
queryPlan.captureMaxTimeMillis=10000
queryPlan.captureMaxCount=10
```

---

## Step 3 - Capture on demand (foreground)

Instead of (or in addition to) the periodic timer, drive capture through
`database.metaInfo()`. This is useful for targeted capture and is how remote tooling
arms specific slow queries by their plan hash.

```java
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;

MetaInfoManager meta = database.metaInfo();

// Phase 1: arm bind capture - either all plans or specific hashes
QueryPlanInit init = new QueryPlanInit();
init.setAll(true);                 // or init.add("<planHash>", 50_000);
init.thresholdMicros(100_000);
List<MetaQueryPlan> armed = meta.queryPlanInit(init);

// ... let the application run so slow executions capture their bind values ...

// Phase 2: collect plans now (runs EXPLAIN)
QueryPlanRequest request = new QueryPlanRequest();
request.maxCount(10);
request.maxTimeMillis(10_000);
request.since(System.currentTimeMillis() - 300_000); // binds at least ~5 min old
List<MetaQueryPlan> plans = meta.queryPlanCollectNow(request);
```

- `QueryPlanInit` arms bind capture. `setAll(true)` arms every plan; `add(hash, micros)`
  arms a specific plan (a hash of `"all"` is treated as all).
- `QueryPlanRequest.since(epochMillis)` ensures the captured bind values have existed for
  a while, so they better represent the slowest executions. `maxCount` / `maxTimeMillis`
  bound the work, mirroring the periodic settings.

`MetaQueryPlan` exposes `beanType()`, `label()`, `profileLocation()`, `sql()`, `hash()`,
`bind()`, `plan()` (the raw EXPLAIN output), `queryTimeMicros()`, `captureCount()`,
`captureMicros()`, and `whenCaptured()`.

---

## Step 4 - EXPLAIN dialect

Ebean chooses the `EXPLAIN` statement per database platform:

| Platform | EXPLAIN used |
|---|---|
| PostgreSQL | `explain (analyze, costs, verbose, buffers) <sql>` |
| YugabyteDB | `explain (analyze, buffers, dist) <sql>` |
| Oracle | `EXPLAIN PLAN FOR <sql>` |
| SQL Server | platform-specific logger |
| H2 / MySQL / other | `explain <sql>` |

Override the prefix with `queryPlanExplain(..)` (or `queryPlan.explain`):

```java
Database.builder()
  .queryPlanExplain("explain (costs, verbose)") // omit ANALYZE on Postgres
  .build();
```

> **Caution (PostgreSQL / Yugabyte):** the default includes `ANALYZE`, which **actually
> executes** the query to produce real timings. For non-idempotent or expensive queries,
> override with a non-ANALYZE `explain` to avoid side effects and extra load.

---

## Related setting: internal plan TTL

`queryPlanTTLSeconds(int)` (default **300**) is a **different** concept — it is the time to
live for Ebean's *internal* query plan (the object that knows how to execute a query, read
the result set and collect metrics). It is not part of EXPLAIN capture, but is set through
the same builder.

---

## Troubleshooting

### No plans are captured

1. `queryPlanEnable(true)` must be set — it is the master switch.
2. `queryPlanThresholdMicros` defaults to `Long.MAX_VALUE`. Lower it, or arm specific
   plans via `QueryPlanInit`, otherwise no execution is ever "slow enough".
3. For periodic capture, also set `queryPlanCapture(true)`.
4. Queries must actually run slower than the threshold to have their binds captured.

### Plans appear but nothing is reported anywhere

No `queryPlanListener` is configured, so plans go to the default `io.ebean.QUERYPLAN`
logger. Set a listener, or enable `INFO` logging for `io.ebean.QUERYPLAN`.

### Capture adds noticeable database load

`EXPLAIN ANALYZE` executes the query. Reduce `queryPlanCaptureMaxCount`, increase
`queryPlanCapturePeriodSecs`, tighten `queryPlanCaptureMaxTimeMillis`, or override
`queryPlanExplain` to a non-ANALYZE form.

### A DtoQuery, SqlQuery or update metric never offers plan capture

Only ORM entity SELECT queries are plan capable. `DtoQuery` (`dto.*`), raw `SqlQuery`
(`sql.query.*`), and write metrics (`orm.update.*`, `iud.*`, `sql.update.*`, `sql.call.*`)
have no bind capture and are intentionally excluded.
