# Ebean ORM Bundle — Setup (Flattened)

> Flattened bundle. Content from source markdown guides is inlined below.

---

## Source: `add-ebean-postgres-maven-pom.md`

# Guide: Add Ebean ORM (PostgreSQL) to an Existing Maven Project — Step 1: POM Setup

## Purpose

This guide provides step-by-step instructions for modifying an existing Maven `pom.xml`
to add Ebean ORM with PostgreSQL support. Follow every step in order. This is Step 1 of 3.

---

## Prerequisites

- An existing Maven project (`pom.xml` already exists)
- Java 11 or higher
- The project does **not** yet include any Ebean dependencies

---

## Step 1 — Define the Ebean version property

Open the module's `pom.xml` (the one that will use Ebean directly, i.e. the module
containing the database configuration and entity classes).

Inside the `<properties>` block, add the `ebean.version` property if it does not
already exist:

```xml
<properties>
    <!-- add this line; use latest stable from https://github.com/ebean-orm/ebean/releases -->
    <ebean.version>17.2.0</ebean.version>
</properties>
```

> If the project has a parent POM that already defines `ebean.version`, skip this step.

---

## Step 2 — Add the PostgreSQL JDBC driver dependency

Inside the `<dependencies>` block, add the PostgreSQL JDBC driver:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.8</version>
</dependency>
```

> Check [Maven Central](https://central.sonatype.com/artifact/org.postgresql/postgresql)
> for the latest version. If the parent POM manages the PostgreSQL version, omit the
> `<version>` tag.

---

## Step 3 — Add the Ebean PostgreSQL platform dependency

Inside the `<dependencies>` block, add the Ebean Postgres platform dependency:

```xml
<dependency>
    <groupId>io.ebean</groupId>
    <artifactId>ebean-postgres</artifactId>
    <version>${ebean.version}</version>
</dependency>
```

This single artifact pulls in the Ebean core, the datasource connection pool
(`ebean-datasource`), and all Postgres-specific support.

---

## Step 4 — Add the ebean-test dependency (test scope)

`ebean-test` configures Ebean for tests and enables automatic Docker container management
for Postgres test instances:

```xml
<!-- test dependencies -->
<dependency>
    <groupId>io.ebean</groupId>
    <artifactId>ebean-test</artifactId>
    <version>${ebean.version}</version>
    <scope>test</scope>
</dependency>
```

---

## Step 5 — Add the ebean-maven-plugin (bytecode enhancement)

Ebean requires bytecode enhancement to provide dirty-checking and lazy-loading.
The `ebean-maven-plugin` performs this enhancement at build time.

Inside the `<build><plugins>` block, add:

```xml
<plugin> <!-- perform ebean enhancement -->
    <groupId>io.ebean</groupId>
    <artifactId>ebean-maven-plugin</artifactId>
    <version>${ebean.version}</version>
    <extensions>true</extensions>
</plugin>
```

---

## Step 6 — Add the querybean-generator annotation processor

The `querybean-generator` annotation processor generates type-safe query bean classes
at compile time. It must be registered as an `annotationProcessorPath` inside
`maven-compiler-plugin`.

### Case A — No existing `maven-compiler-plugin` configuration

Add the full plugin entry to `<build><plugins>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.15.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path> <!-- generate ebean query beans -->
                <groupId>io.ebean</groupId>
                <artifactId>querybean-generator</artifactId>
                <version>${ebean.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Case B — `maven-compiler-plugin` already exists with `<annotationProcessorPaths>`

Locate the existing `<annotationProcessorPaths>` block inside the existing
`maven-compiler-plugin` entry and add the new `<path>` inside it. Do **not** add a
second `<configuration>` block or a second `<annotationProcessorPaths>` block.

Example — if the existing block already has a path for, say, `avaje-nima-generator`:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>io.avaje</groupId>
        <artifactId>avaje-nima-generator</artifactId>
        <version>${avaje-nima.version}</version>
    </path>
    <!-- ADD the new path here, inside the existing block -->
    <path>
        <groupId>io.ebean</groupId>
        <artifactId>querybean-generator</artifactId>
        <version>${ebean.version}</version>
    </path>
</annotationProcessorPaths>
```

---

## Verification

Run the following to confirm the POM is valid and the project compiles:

```bash
mvn compile -pl <your-module-name>
```

Expected result: `BUILD SUCCESS` with no errors from Ebean or the annotation processor.

---

## Next Step

Proceed to **Step 2: Test container setup**
(`add-ebean-postgres-test-container.md`) to wire an injectable test `Database`
backed by `ebean-test` containers. Verify with `mvn verify` before continuing
to production database configuration.

---

## Source: `add-ebean-postgres-test-container.md`

# Guide: Add Ebean ORM (PostgreSQL) to an Existing Maven Project — Step 3: Test Container Setup

## Purpose

This guide provides step-by-step instructions for setting up a PostgreSQL Docker
container for tests using `ebean-test-containers`, exposing an `io.ebean.Database`
bean via an Avaje Inject `@TestScope @Factory` class. This is Step 2 of 3.

Complete this step before configuring the production database in Step 3. Getting
the test container working first gives you a fast feedback loop — you can verify
entity changes compile, enhance, and persist correctly with `mvn verify` before
wiring up production datasource configuration.

Two variants are covered:
- **Variant A** — plain PostgreSQL
- **Variant B** — PostgreSQL with PostGIS extension

---

## Prerequisites

- **Step 1 complete**: `pom.xml` includes `ebean-postgres`, `ebean-maven-plugin`,
  `querybean-generator`, and **`ebean-test`** as a test-scoped dependency
  (see `add-ebean-postgres-maven-pom.md`)
- **Avaje Inject** is on the classpath with test support (`io.avaje:avaje-inject-test`)
- **Docker** is installed and running on the developer machine

---

## Overview: Declarative vs Programmatic approach

`ebean-test` supports two ways to configure the test database:

| Approach | How | Best for |
|----------|-----|---------|
| **Declarative** | `src/test/resources/application-test.yaml` | Simple projects with no DI, no image mirrors |
| **Programmatic** | `@TestScope @Factory` class | Avaje Inject tests, private image mirrors (ECR), more control |

This guide uses the **programmatic approach** because it integrates naturally with
Avaje Inject, allows a private mirror to be specified (useful in CI with ECR or similar),
and makes the `Database` injectable into tests.

---

## Step 1 — Verify ebean-test is a test dependency

Confirm the following is present in `pom.xml` (added in Step 1):

```xml
<dependency>
    <groupId>io.ebean</groupId>
    <artifactId>ebean-test</artifactId>
    <version>${ebean.version}</version>
    <scope>test</scope>
</dependency>
```

`ebean-test` transitively brings in `ebean-test-containers` which provides
`PostgresContainer` and `PostgisContainer`.

---

## Step 2 — Create a `@TestScope @Factory` class

Create a new class in the test source tree (e.g., `src/test/java/.../testconfig/TestConfiguration.java`).
Annotate it with `@TestScope` and `@Factory` so Avaje Inject uses it only in tests.

```java
package com.example.testconfig;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;
import io.ebean.Database;

@TestScope
@Factory
class TestConfiguration {
    // bean methods added in the steps below
}
```

---

## Step 3 — Add a container bean and a Database bean

### Variant A — Plain PostgreSQL

```java
import io.ebean.test.containers.PostgresContainer;

@TestScope
@Factory
class TestConfiguration {

    @Bean
    PostgresContainer postgres() {
        return PostgresContainer.builder("17")   // Postgres image version
            .dbName("my_app")                    // database to create inside the container
            .build()
            .start();
    }

    @Bean
    Database database(PostgresContainer container) {
        return container.ebean()
            .builder()
            .build();
    }
}
```

### Variant B — PostGIS (PostgreSQL + PostGIS extension)

Use `PostgisContainer` instead of `PostgresContainer`. The default image is
`ghcr.io/baosystems/postgis:{version}` and the extensions `hstore`, `pgcrypto`,
and `postgis` are installed automatically.

```java
import io.ebean.test.containers.PostgisContainer;

@TestScope
@Factory
class TestConfiguration {

    @Bean
    PostgisContainer postgres() {
        return PostgisContainer.builder("17")    // PostGIS image version (Postgres 17)
            .dbName("my_app")
            .build()
            .start();
    }

    @Bean
    Database database(PostgisContainer container) {
        return container.ebean()
            .builder()
            .build();
    }
}
```

### Key differences from Variant A

| | PostgresContainer | PostgisContainer |
|---|---|---|
| Docker image | `postgres:{version}` | `ghcr.io/baosystems/postgis:{version}` |
| Default extensions | `hstore, pgcrypto` | `hstore, pgcrypto, postgis` |
| Default port | 6432 | 6432 |
| Optional LW mode | — | `.useLW(true)` (see Optional section) |

---

## Step 4 — Write a test

Annotate the test class with `@InjectTest` and inject `Database` with `@Inject`:

```java
package com.example.testconfig;

import io.avaje.inject.test.InjectTest;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@InjectTest
class DatabaseTest {

    @Inject
    Database database;

    @Test
    void database_isAvailable() {
        assertThat(database).isNotNull();
    }
}
```

---

## Verification

Run the tests:

```bash
mvn verify
```

Expected log output confirming the container started and Ebean connected:

```
INFO  Container ut_postgres running with port:6432 ...
INFO  connectivity confirmed for ut_postgres
INFO  DataSourcePool [my_app] autoCommit[false] ...
INFO  DatabasePlatform name:my_app platform:postgres
INFO  Executing db-create-all.sql - ...
```

**Important:** Verify this step passes with `mvn verify` before proceeding to
Step 3 (production database configuration). A working test container gives you
a fast feedback loop for all subsequent entity and query changes.

---

## Optional configurations

### Image mirror (for CI / private registry)

If CI builds pull images from a private registry (e.g., AWS ECR) instead of Docker Hub
or GitHub Container Registry, specify a mirror. The mirror is **only used in CI** —
it is ignored on local developer machines (where Docker Hub / GHCR is used directly).

```java
@Bean
PostgresContainer postgres() {
    return PostgresContainer.builder("16")
        .dbName("my_app")
        .mirror("123456789.dkr.ecr.ap-southeast-2.amazonaws.com/mirrored")
        .build()
        .start();
}
```

Alternatively, set the mirror globally via a system property or
`ebean.test.containers.mirror` in a properties file, avoiding code changes per project.

### Read-only datasource (for tests using read-replica simulation)

Call `.autoReadOnlyDataSource(true)` on the `DatabaseBuilder` to automatically
create a second read-only datasource pointing at the same container:

```java
@Bean
Database database(PostgresContainer container) {
    return container.ebean()
        .builder()
        .autoReadOnlyDataSource(true)   // test read-only queries against same container
        .build();
}
```

### Dump metrics on shutdown

Useful for performance analysis during test runs:

```java
@Bean
Database database(PostgresContainer container) {
    return container.ebean()
        .builder()
        .dumpMetricsOnShutdown(true)
        .dumpMetricsOptions("loc,sql,hash")
        .build();
}
```

### PostGIS: LW mode (HexWKB)

For PostGIS with DriverWrapperLW (HexWKB binary geometry encoding), set `.useLW(true)`.
This switches the JDBC URL prefix to `jdbc:postgresql_lwgis://` and requires the
`net.postgis:postgis-jdbc` dependency on the test classpath:

```xml
<!-- add to pom.xml test dependencies when using useLW(true) -->
<dependency>
    <groupId>net.postgis</groupId>
    <artifactId>postgis-jdbc</artifactId>
    <version>2024.1.0</version>
    <scope>test</scope>
</dependency>
```

```java
@Bean
PostgisContainer postgres() {
    return PostgisContainer.builder("16")
        .dbName("my_app")
        .useLW(true)    // use HexWKB + DriverWrapperLW
        .build()
        .start();
}
```

> **Note**: LW mode is not required for most PostGIS use cases. Only enable it if
> your entities use binary geometry types (e.g., `net.postgis.jdbc.geometry.Geometry`)
> that require the `DriverWrapperLW` driver.

---

## Keeping the container running (local development)

By default, `ebean-test` stops the Docker container when tests finish. To keep it
running between test runs (much faster for local development), create a marker file:

```bash
mkdir -p ~/.ebean && touch ~/.ebean/ignore-docker-shutdown
```

On CI servers, omit this file so containers are cleaned up after each build.

---

## Source: `add-ebean-postgres-database-config.md`

# Guide: Add Ebean ORM (PostgreSQL) to an Existing Maven Project — Step 2: Database Configuration

## Purpose

This guide provides step-by-step instructions for configuring an Ebean `Database` bean
using **Avaje Inject** (`@Factory` / `@Bean`), backed by a PostgreSQL datasource built
with Ebean's `DataSourceBuilder`. Follow every step in order. This is Step 3 of 3.

---

## Prerequisites

- **Step 1 complete**: `pom.xml` already includes `ebean-postgres`, `ebean-maven-plugin`,
  and `querybean-generator` (see `add-ebean-postgres-maven-pom.md`)
- **Step 2 complete**: Test container setup is working and `mvn verify` passes
  (see `add-ebean-postgres-test-container.md`)
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

## Related

The test container setup (Step 2) should already be complete and passing
before this step. See `add-ebean-postgres-test-container.md`.
