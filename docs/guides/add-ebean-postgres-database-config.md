# Guide: Add Ebean ORM (PostgreSQL) to an Existing Maven Project — Step 2: Database Configuration

## Purpose

This guide provides step-by-step instructions for configuring an Ebean `Database` bean
using **Avaje Inject** (`@Factory` / `@Bean`), backed by a PostgreSQL datasource built
with Ebean's `DataSourceBuilder`. Follow every step in order. This is Step 2 of 3.

---

## Prerequisites

- **Step 1 complete**: `pom.xml` already includes `ebean-postgres`, `ebean-maven-plugin`,
  and `querybean-generator` (see `add-ebean-postgres-maven-pom.md`)
- **Avaje Inject** is on the classpath (e.g. `io.avaje:avaje-inject`)
- A configuration source is available at runtime (e.g. `avaje-config` reading
  `application.yml` or environment variables)
- The following configuration keys are resolvable at runtime (adapt names to your project):
  | Key | Description |
  |-----|-------------|
  | `db_url` | JDBC URL for the master/write connection |
  | `db_user` | Database username |
  | `db_pass` | Database password |
  | `db_master_min_connections` | Minimum pool size (default: 1) |
  | `db_master_initial_connections` | Initial pool size at startup — set high to pre-warm on pod start (see K8s note below) |
  | `db_master_max_connections` | Maximum pool size (default: 200) |

---

## Step 1 — Locate or create the `@Factory` class

Look for an existing Avaje Inject `@Factory`-annotated class in the project
(often named `AppConfig`, `DatabaseConfig`, or similar). If one exists, add the new
`@Bean` method to it. If none exists, create one:

```java
package com.example.configuration;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
class DatabaseConfig {
    // beans will be added in the steps below
}
```

---

## Step 2 — Add the `Database` bean method (minimal — master datasource only)

Add the following `@Bean` method to the `@Factory` class. This creates an Ebean
`Database` backed by a single master (read-write) PostgreSQL datasource.

```java
import io.ebean.Database;
import io.ebean.datasource.DataSourceBuilder;

@Bean
Database database() {
    var dataSource = DataSourceBuilder.create()
        .url(/* resolve from config, e.g.: */ Config.get("db_url"))
        .username(Config.get("db_user"))
        .password(Config.get("db_pass"))
        .driver("org.postgresql.Driver")
        .schema("myschema")                    // set to your target schema
        .applicationName("my-app")             // visible in pg_stat_activity
        .minConnections(Config.getInt("db_master_min_connections", 1))
        .initialConnections(Config.getInt("db_master_initial_connections", 10))
        .maxConnections(Config.getInt("db_master_max_connections", 200));

    return Database.builder()
        .name("db")                            // logical name for this Database instance
        .dataSourceBuilder(dataSource)
        .build();
}
```

### Field guidance

| Field | Notes |
|-------|-------|
| `url` | Full JDBC URL, e.g. `jdbc:postgresql://host:5432/dbname` |
| `schema` | The Postgres schema Ebean should use (omit if using `public`) |
| `applicationName` | Shown in `pg_stat_activity.application_name`; helps with DB-side diagnostics |
| `name("db")` | Logical Ebean database name; relevant if multiple Database instances exist |
| `minConnections` | Connections kept open at all times; pool will not shrink below this |
| `initialConnections` | Connections opened at startup; see K8s warm-up note below |
| `maxConnections` | Hard upper limit on concurrent connections |

### Connection pool sizing for Kubernetes (and similar orchestrated environments)

When a pod starts in Kubernetes it will receive live traffic as soon as it passes
readiness checks — often before the connection pool has had a chance to grow to handle
the load. This can cause latency spikes on the first wave of requests while the pool
expands one connection at a time.

Use `initialConnections` to **pre-warm the pool at startup** so it is already sized
for peak load when the pod goes live:

```
minConnections:     2    ← floor; pool will shrink back here when idle
initialConnections: 20   ← opened at pod start, before first request arrives
maxConnections:     50   ← hard ceiling
```

The lifecycle is:
1. **Pod starts** — pool opens `initialConnections` connections immediately.
2. **Pod receives traffic** — pool is already at capacity; no growth latency.
3. **Traffic drops** — idle connections are closed; pool trims back toward `minConnections`.
4. **Next traffic spike** — pool grows again up to `maxConnections` on demand.

Set `initialConnections` to a value high enough that the pool does not need to grow
during the first minute of live traffic. A common starting point is 50–75% of
`maxConnections`.

---

## Step 3 — Inject configuration via a constructor or config helper (recommended)

Rather than calling `Config.get(...)` inline, inject a typed config helper or the
Avaje `Configuration` bean if one is available. This makes the factory testable and
keeps the wiring explicit. For example:

```java
@Bean
Database database(Configuration config) {
    String url  = config.get("db_url");
    String user = config.get("db_user");
    String pass = config.get("db_pass");
    int    min  = config.getInt("db_master_min_connections", 1);
    int    init = config.getInt("db_master_initial_connections", 10);
    int    max  = config.getInt("db_master_max_connections", 200);

    var dataSource = DataSourceBuilder.create()
        .url(url)
        .username(user)
        .password(pass)
        .driver("org.postgresql.Driver")
        .schema("myschema")
        .applicationName("my-app")
        .minConnections(min)
        .initialConnections(init)
        .maxConnections(max);

    return Database.builder()
        .name("db")
        .dataSourceBuilder(dataSource)
        .skipDataSourceCheck(true)
        .build();
}
```

If the project has a dedicated config-wrapper class (a `@Component` that reads config
keys), accept it as a parameter instead of `Configuration`.

---

## Step 4 (Optional) — Add a read-only datasource

For production services that have a separate read-replica, add a second
`DataSourceBuilder` for read-only queries and wire it via
`readOnlyDataSourceBuilder(...)`. The read-only datasource:

- Uses `readOnly(true)` and `autoCommit(true)` (Ebean routes read queries there automatically)
- Typically has a higher max connection count than the master
- Benefits from a prepared-statement cache (`pstmtCacheSize`)

```java
@Bean
Database database(Configuration config) {
    String masterUrl   = config.get("db_url");
    String readOnlyUrl = config.get("db_url_readonly");
    String user        = config.get("db_user");
    String pass        = config.get("db_pass");

    var masterDataSource = buildDataSource(user, pass)
        .url(masterUrl)
        .minConnections(config.getInt("db_master_min_connections", 1))
        .initialConnections(config.getInt("db_master_initial_connections", 10))
        .maxConnections(config.getInt("db_master_max_connections", 50));

    var readOnlyDataSource = buildDataSource(user, pass)
        .url(readOnlyUrl)
        .readOnly(true)
        .autoCommit(true)
        .pstmtCacheSize(250)          // cache up to 250 prepared statements per connection
        .maxInactiveTimeSecs(600)     // close idle connections after 10 minutes
        .minConnections(config.getInt("db_readonly_min_connections", 2))
        .initialConnections(config.getInt("db_readonly_initial_connections", 10))
        .maxConnections(config.getInt("db_readonly_max_connections", 200));

    return Database.builder()
        .name("db")
        .dataSourceBuilder(masterDataSource)
        .readOnlyDataSourceBuilder(readOnlyDataSource)
        .build();
}

private static DataSourceBuilder buildDataSource(String user, String pass) {
    return DataSourceBuilder.create()
        .username(user)
        .password(pass)
        .driver("org.postgresql.Driver")
        .schema("myschema")
        .applicationName("my-app")
        .addProperty("prepareThreshold", "2");   // PostgreSQL: server-side prepared statements
}
```

### Additional configuration keys for the read-only datasource

| Key | Description | Default |
|-----|-------------|---------|
| `db_url_readonly` | JDBC URL for the read replica | — |
| `db_master_initial_connections` | Initial master pool size at startup | 10 |
| `db_readonly_min_connections` | Minimum pool size | 2 |
| `db_readonly_initial_connections` | Initial pool size at startup | same as min |
| `db_readonly_max_connections` | Maximum pool size | 20 |

---

## Step 5 (Optional) — Enable the migration runner

If the project uses Ebean's built-in DB migration runner to apply SQL migrations on
startup, enable it on the `DatabaseBuilder`:

```java
return Database.builder()
    .name("db")
    .dataSourceBuilder(dataSource)
    .runMigration(true)          // run pending migrations on startup
    .build();
```

This is equivalent to setting `ebean.migration.run=true` in `application.properties`
but is preferred because it keeps all database configuration in one place. To make it
conditional (e.g. only in non-production environments):

```java
.runMigration(config.getBoolean("db.runMigrations", false))
```

See the DB migration generation guide (`add-ebean-db-migration-generation.md`) for
full details on generating and managing migration files.

---

## See Also

For advanced connection pool configuration, production deployment patterns, and connection
validation best practices, see the [ebean-datasource guides](https://github.com/ebean-orm/ebean-datasource/tree/master/docs/guides/):

- **[Creating DataSource Pools](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/create-datasource-pool.md)** — Covers read-only pools (`readOnly(true)` + `autoCommit(true)`), Kubernetes deployment strategies using `initialConnections`, and AWS Lambda optimization
- **[AWS Aurora Read-Write Split](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/aws-aurora-read-write-split.md)** — Setting up dual DataSources with Aurora reader and writer endpoints, including Ebean secondary datasource routing
- **[Connection Validation Best Practices](https://github.com/ebean-orm/ebean-datasource/blob/master/docs/guides/connection-validation-best-practices.md)** — Why `Connection.isValid()` is the recommended default and when (rarely) explicit `heartbeatSql` is needed

---

## Verification

1. Start the application (or run `mvn test -pl <your-module>`).
2. Look for log output similar to:

   ```
   INFO  o.a.datasource.pool.ConnectionPool - DataSourcePool [db] autoCommit[false] min[1] max[5]
   INFO  io.ebean.internal.DefaultContainer - DatabasePlatform name:db platform:postgres
   ```

3. If you see `DataSourcePool` and `DatabasePlatform` log lines, Ebean is connected and
   the database bean is wired correctly.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| `ClassNotFoundException: org.postgresql.Driver` | PostgreSQL JDBC driver missing | Add `org.postgresql:postgresql` dependency (see Step 1 guide) |
| `Cannot connect to database` at startup | DB unreachable but `skipDataSourceCheck` is `false` | Set `.skipDataSourceCheck(true)` |
| Ebean enhancement warnings in logs | `ebean-maven-plugin` not configured | Complete Step 1 guide |
| `NullPointerException` reading config key | Config key not defined | Add the key to `application.yml` or environment |

---

## Next Step

Proceed to **Step 3: Test container setup**
(`add-ebean-postgres-test-container.md`) to wire an injectable test `Database`
backed by `ebean-test` containers.
