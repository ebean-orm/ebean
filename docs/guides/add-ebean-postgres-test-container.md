# Guide: Add Ebean ORM (PostgreSQL) to an Existing Maven Project — Step 3: Test Container Setup

## Purpose

This guide provides step-by-step instructions for setting up a PostgreSQL Docker
container for tests using `ebean-test-containers`, exposing an `io.ebean.Database`
bean via an Avaje Inject `@TestScope @Factory` class. This is Step 3 of 3.

Two variants are covered:
- **Variant A** — plain PostgreSQL
- **Variant B** — PostgreSQL with PostGIS extension

---

## Prerequisites

- **Step 1 complete**: `pom.xml` includes `ebean-postgres`, `ebean-maven-plugin`,
  `querybean-generator`, and **`ebean-test`** as a test-scoped dependency
  (see `add-ebean-postgres-maven-pom.md`)
- **Step 2 complete**: A production `Database` bean exists (see `add-ebean-postgres-database-config.md`)
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
mvn test -pl <your-module>
```

Expected log output confirming the container started and Ebean connected:

```
INFO  Container ut_postgres running with port:6432 ...
INFO  connectivity confirmed for ut_postgres
INFO  DataSourcePool [my_app] autoCommit[false] ...
INFO  DatabasePlatform name:my_app platform:postgres
INFO  Executing db-create-all.sql - ...
```

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
