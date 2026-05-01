# Ebean ORM Bundle — DB Migrations (Flattened)

> Flattened bundle. Content from source markdown guides is inlined below.

---

## Source: `add-ebean-db-migration-generation.md`

# Guide: Add Ebean Database Migration Generation to an Existing Maven Project

## Purpose

This guide provides step-by-step instructions for adding Ebean DB migration generation
to an existing Maven project that already uses Ebean ORM. Ebean generates migrations by
performing a diff of the current entity model against the previously recorded model state,
producing platform-specific DDL SQL scripts.

These instructions are designed for AI agents and developers to follow precisely.

---

## Prerequisites

- An existing Maven project with Ebean ORM configured (entity beans present)
- `ebean-test` is already a test-scoped dependency (from POM setup guide)
- The project targets PostgreSQL (adjust `Platform.POSTGRES` for other databases)

---

## Step 1 — Verify migration dependencies

### Generation tooling (`ebean-ddl-generator`)

`ebean-test` (already present as a test dependency) transitively includes
`ebean-ddl-generator`, which provides the `DbMigration` class. No additional dependency
is required for generation.

### Runtime migration runner (`ebean-migration`)

`ebean-migration` is the library that runs migrations on application startup.
It is typically included **transitively** via `io.ebean:ebean-postgres` (or the
equivalent platform dependency). Verify it is on the classpath by running:

```bash
mvn dependency:tree | grep ebean-migration
```

If it is **not** present transitively, add it explicitly as a compile-scope dependency:

```xml
<dependency>
    <groupId>io.ebean</groupId>
    <artifactId>ebean-migration</artifactId>
    <version>${ebean.version}</version>
</dependency>
```

---

## Step 2 — Create `GenerateDbMigration.java`

Create the following class in `src/test/java/main/`. This `main` method is run manually
by a developer (or AI agent) whenever entity beans change and a new migration is needed.

```java
package main;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

import java.io.IOException;

/**
 * Generate the next database migration based on a diff of the entity model.
 * Run this main method after making entity bean changes to produce the migration SQL.
 */
public class GenerateDbMigration {

    public static void main(String[] args) throws IOException {

        DbMigration migration = DbMigration.create();
        migration.setPlatform(Platform.POSTGRES);

        migration.setVersion("1.1");           // set to the next migration version
        migration.setName("add-customer");     // short description of the change

        migration.generateMigration();
    }
}
```

### Version naming convention

Ebean supports two common version formats — choose one and apply it consistently:

| Format | Example | Notes |
|--------|---------|-------|
| **Date-based** | `20240820` | `YYYYMMDD`; used when changes are tied to dates; easily sortable |
| **Semantic** | `1.1`, `1.2`, `2.0` | Traditional versioning; useful for release-based workflows |

The version controls execution order — Ebean runs migrations in ascending version order.

### Name convention

The `name` should be a short, lowercase, hyphenated description of the change:
- `add-customer-email`
- `rename-machine-type`
- `drop-unused-columns`

---

## Step 3 — Configure the output path (if needed)

By default, migration files are written to `src/main/resources/dbmigration/` relative
to the **current working directory** when `generateMigration()` is called. This is
usually the module root, which is correct for single-module projects.

For **multi-module projects** where `GenerateDbMigration` is in a submodule but the
resources directory is at a different relative path, specify it explicitly:

```java
// Relative path from the working directory (project root) to the module's resources
migration.setPathToResources("my-module/src/main/resources");
```

---

## Step 4 — Run `GenerateDbMigration` to produce the first migration

Run the `main` method via the IDE or Maven:

```bash
# Run via Maven exec plugin (or use IDE run configuration)
mvn test-compile exec:java \
    -Dexec.mainClass="main.GenerateDbMigration" \
    -Dexec.classpathScope="test" \
    -pl <your-module>
```

Ebean migration generation runs in **offline mode** — no database connection is required.

### Expected output files

After running, two files are created per migration in `src/main/resources/dbmigration/`:

```
src/main/resources/dbmigration/
  1.1__add-customer.sql           ← DDL SQL to apply (commit this)
  model/
    1.1__add-customer.model.xml   ← logical model diff XML (commit this)
```

Both files must be committed to source control. The `.model.xml` file records the
logical state of the diff and is used by subsequent migration generations to determine
what has changed.

If **no entity beans have changed** since the last migration, the command outputs:
```
DbMigration - no changes detected - no migration written
```

---

## Step 5 — Enable the migration runner

Configure Ebean to run pending migrations automatically on application startup.

### Preferred approach — programmatic via `DatabaseBuilder`

Set `runMigration(true)` directly on the `DatabaseBuilder` when constructing
the `Database` bean. This is the preferred approach as it is explicit, co-located with
the database configuration, and does not rely on external property files.

In the `@Factory` class that builds the `Database` bean (see the database configuration
guide), add `.runMigration(true)` to the builder chain:

```java
@Bean
Database database(ConfigWrapper config) {
    var dataSource = DataSourceBuilder.create()
        .url(config.getDatabaseUrl())
        .username(config.getDatabaseUser())
        .password(config.getDatabasePassword())
        // ... other datasource settings ...
        ;

    return Database.builder()
        .name("db")
        .dataSourceBuilder(dataSource)
        .runMigration(true)          // run pending migrations on startup
        .build();
}
```

If migrations should only run in certain environments (e.g., not in production, or
only when a config flag is set), make it conditional:

```java
.runMigration(config.isRunMigrations())   // driven by config value
```

### Alternative — via application properties

If programmatic configuration is not available or not preferred, set the property
in `src/main/resources/application.properties`:

```properties
ebean.migration.run=true
```

Or in `src/main/resources/application.yaml`:
```yaml
ebean:
  migration:
    run: true
```

For a **named database** (i.e., `Database.builder().name("mydb")`), use the database
name in the property key:

```properties
ebean.mydb.migration.run=true
```

### What the runner does at startup

When migration running is enabled, Ebean will on each application start:
1. Look at the migrations in `src/main/resources/dbmigration/`
2. Compare against the `db_migration` table (created automatically on first run)
3. Apply any migrations that have not yet been executed, in version order
4. Record each successfully applied migration in `db_migration`

---

## Step 6 — Commit the migration files

Add both generated files to source control:

```bash
git add src/main/resources/dbmigration/1.1__add-customer.sql
git add src/main/resources/dbmigration/model/1.1__add-customer.model.xml
git commit -m "Add db migration 1.1: add-customer"
```

---

## Ongoing workflow — generating subsequent migrations

For each future set of entity bean changes:

1. Make changes to the entity bean classes
2. Update `GenerateDbMigration.java` with the **new version** and **new name**:
   ```java
   migration.setVersion("1.2");
   migration.setName("add-address-table");
   ```
3. Run the `main` method — a new `.sql` and `.model.xml` pair is written
4. Review the generated `.sql` to confirm it reflects the intended changes
5. Commit both files

---

## Understanding the output files

### Apply SQL (`.sql`)

The apply SQL file contains the DDL that will be executed against the database:

```sql
-- apply changes
alter table customer add column email varchar(255);
```

### Model XML (`.model.xml`)

The model XML records the logical diff in a database-agnostic format. Ebean uses
this file on the next generation run to determine what has already been captured.
It is not executed against the database.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<migration xmlns="http://ebean-orm.github.io/xml/ns/dbmigration">
  <changeSet type="apply">
    <addColumn tableName="customer">
      <column name="email" type="varchar(255)"/>
    </addColumn>
  </changeSet>
</migration>
```

---

## Optional configurations

### Multiple database platforms

To generate migrations for multiple platforms simultaneously, use `addPlatform()`
instead of `setPlatform()`:

```java
migration.addPlatform(Platform.POSTGRES);
migration.addPlatform(Platform.SQLSERVER17);
migration.addPlatform(Platform.MYSQL);
```

Each platform gets its own subdirectory under `dbmigration/`.

### Include index

When enabled the migration generation also generates a file that contains
all the migrations and their associated hashes. This is a performance
optimisation (that will become the default) and means that the migration
runner just needs to read the one resource and has the pre-computed hash
values (so does not need to read each migration resource and compute the
hash for each of those at runtime).

```java
migration.setIncludeIndex(true);
```

### Strict mode

Strict mode (on by default) errors if there are any pending drops not yet applied.
Set to `false` to allow generation to proceed regardless:

```java
migration.setStrictMode(false);
```

### Applying pending drops

Destructive changes (drop column, drop table) are **not** included in the apply
SQL by default — they are recorded as `pendingDrops` in the model XML. This allows
the application to be deployed without immediately dropping columns (important for
rolling deployments).

The migration runner logs a message when pending drops exist:
```
INFO  DbMigration - Pending un-applied drops in versions [1.1]
```

When ready to apply the drops, set `setGeneratePendingDrop` to the version that
contains the pending drops:

```java
migration.setVersion("1.3");
migration.setName("drop-pending-from-1.1");
migration.setGeneratePendingDrop("1.1");  // apply drops recorded in version 1.1
migration.generateMigration();
```

### Custom dbSchema

If the project uses a named Postgres schema (set via `ebean.dbSchema` in
`application.properties`), no additional configuration is needed in
`GenerateDbMigration` — Ebean picks up the schema from the application config
automatically when running in offline mode.

```properties
# application.properties
ebean.dbSchema=myschema
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| `no changes detected - no migration written` | Entity beans unchanged since last migration | Make entity bean changes first, then re-run |
| `DbMigration - Pending un-applied drops` | A previous migration has drops not yet applied | Either suppress with `setStrictMode(false)` or apply drops with `setGeneratePendingDrop(...)` |
| Generated SQL is empty or wrong | Wrong working directory path | Set `setPathToResources(...)` to the correct module-relative path |
| `ClassNotFoundException` for entity classes | Test classpath not including main classes | Ensure `exec.classpathScope=test` or run via IDE with test classpath |
| Migrations not running on startup | Property key wrong or `ebean-migration` missing | Verify `ebean[.name].migration.run=true` and that `ebean-migration` is on the classpath |
