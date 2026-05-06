# Guide: Add Ebean ORM (PostgreSQL) to an Existing Maven Project - Step 2: Test Container Setup

## Purpose

This guide provides step-by-step instructions for setting up a PostgreSQL Docker
container for tests, exposing an `io.ebean.Database` instance for use in test
classes. This is Step 2 of 3.

Complete this step before configuring the production database in Step 3. Getting
the test container working first gives you a fast feedback loop - you can verify
entity changes compile, enhance, and persist correctly with `mvn verify` before
wiring up production datasource configuration.

---

## Prerequisites

- **Step 1 complete**: `pom.xml` includes `ebean-postgres`, `ebean-maven-plugin`,
  `querybean-generator`, and **`ebean-test`** as a test-scoped dependency
  (see `add-ebean-postgres-maven-pom.md`)
- **Step 0 answers recorded**: DI framework choice and PostGIS requirement
- **Docker** is installed and running on the developer machine

---

## Overview: Choosing your approach

The approach depends on the DI framework choice made in Step 0:

| DI framework | Approach | How |
|--------------|----------|-----|
| **Avaje Inject** | Programmatic | `@TestScope @Factory` class with injectable `Database` bean |
| **Spring** | Programmatic | `@TestConfiguration` class with `@Bean` methods |
| **None** | Declarative | `application-test.yaml` + plain JUnit test |

Follow the path that matches your choice below.

---

## Path A — Programmatic with Avaje Inject (recommended)

This approach uses `@TestScope @Factory` to expose the container and `Database`
as injectable beans. It offers more control (image mirrors, custom config) and
makes `Database` directly injectable into test classes.

### A.1 — Verify Avaje Inject test dependencies

Confirm the following are present in `pom.xml` (in addition to `ebean-test`):

```xml
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-inject</artifactId>
    <version>${avaje-inject.version}</version>
</dependency>
<dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-inject-test</artifactId>
    <version>${avaje-inject.version}</version>
    <scope>test</scope>
</dependency>
```

And the `avaje-inject-generator` annotation processor in `maven-compiler-plugin`:

```xml
<path>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-inject-generator</artifactId>
    <version>${avaje-inject.version}</version>
</path>
```

### A.2 — Create a `@TestScope @Factory` class

Create a new class in the test source tree (e.g., `src/test/java/.../testconfig/TestConfiguration.java`):

```java
package com.example.testconfig;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;
import io.ebean.Database;

@TestScope
@Factory
class TestConfiguration {
    // bean methods added below
}
```

### A.3 — Add a container bean and a Database bean

#### Plain PostgreSQL

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

#### PostGIS (PostgreSQL + PostGIS extension)

Use `PostgisContainer` instead. The default image is
`ghcr.io/baosystems/postgis:{version}` and the extensions `hstore`, `pgcrypto`,
and `postgis` are installed automatically.

```java
import io.ebean.test.containers.PostgisContainer;

@TestScope
@Factory
class TestConfiguration {

    @Bean
    PostgisContainer postgres() {
        return PostgisContainer.builder("17")
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

#### Key differences

| | PostgresContainer | PostgisContainer |
|---|---|---|
| Docker image | `postgres:{version}` | `ghcr.io/baosystems/postgis:{version}` |
| Default extensions | `hstore, pgcrypto` | `hstore, pgcrypto, postgis` |
| Default port | 6432 | 6432 |
| Optional LW mode | — | `.useLW(true)` (see Optional section) |

### A.4 — Write a test

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

### A.5 — Verify

```bash
mvn verify
```

Expected log output:

```
INFO  Container ut_postgres running with port:6432 ...
INFO  connectivity confirmed for ut_postgres
INFO  DataSourcePool [my_app] autoCommit[false] ...
INFO  DatabasePlatform name:my_app platform:postgres
INFO  Executing db-create-all.sql - ...
```

**Important:** Verify this step passes with `mvn verify` before proceeding to
Step 3 (production database configuration).

---

## Path B — Programmatic with Spring

Use Spring’s `@TestConfiguration` to provide the container and `Database` beans.

### B.1 — Create a `@TestConfiguration` class

```java
package com.example.testconfig;

import io.ebean.Database;
import io.ebean.test.containers.PostgresContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class TestDatabaseConfig {

    @Bean
    PostgresContainer postgres() {
        return PostgresContainer.builder("17")
            .dbName("my_app")
            .build()
            .start();
    }

    @Primary
    @Bean
    Database database(PostgresContainer container) {
        return container.ebean()
            .builder()
            .build();
    }
}
```

For PostGIS, use `PostgisContainer` instead (same pattern as Path A).

### B.2 — Write a test

```java
package com.example;

import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseTest {

    @Autowired
    Database database;

    @Test
    void database_isAvailable() {
        assertThat(database).isNotNull();
    }
}
```

### B.3 — Verify

Run `mvn verify` and confirm the same log output as Path A.

---

## Path C — Declarative (no DI framework)

This is the simplest approach but offers less control. `ebean-test` reads a
YAML config file and automatically manages the Docker container and `Database`
instance. Use this when the project has no DI framework.

### C.1 — Create `application-test.yaml`

Create `src/test/resources/application-test.yaml`:

```yaml
ebean:
  test:
    platform: postgres
    ddlMode: dropCreate
    dbName: my_app
```

For PostGIS, use `platform: postgis` instead.

### C.2 — Write a test

Use `DB.getDefault()` to obtain the `Database` instance:

```java
package com.example;

import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTest {

    @Test
    void database_isAvailable() {
        Database database = DB.getDefault();
        assertThat(database).isNotNull();
    }
}
```

### C.3 — Verify

```bash
mvn verify
```

Expected log output:

```
INFO  Container ut_postgres running with port:6432 ...
INFO  connectivity confirmed for ut_postgres
INFO  DataSourcePool [my_app] autoCommit[false] ...
INFO  DatabasePlatform name:my_app platform:postgres
```

**Important:** Verify this passes before proceeding to Step 3.

Skip to [Optional configurations](#optional-configurations) or proceed to Step 3.

---

## Optional configurations

### Image mirror (for CI / private registry)

If CI builds pull images from a private registry (e.g., AWS ECR) instead of Docker Hub
or GitHub Container Registry, specify a mirror. The mirror is **only used in CI** -
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
