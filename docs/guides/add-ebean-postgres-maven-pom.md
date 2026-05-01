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

Run the following to confirm the POM is valid and both main and test sources compile:

```bash
mvn test-compile
```

Expected result: `BUILD SUCCESS` with no errors from Ebean or the annotation processor.
Using `test-compile` rather than `compile` ensures test dependencies and test
source files are also verified.

---

## Next Step

Proceed to **Step 2: Test container setup**
(`add-ebean-postgres-test-container.md`) to wire an injectable test `Database`
backed by `ebean-test` containers. Verify with `mvn verify` before continuing
to production database configuration.
