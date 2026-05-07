# Guide: Migrate from `DatabaseConfig` / `DatabaseFactory` to `Database.builder()`

## Purpose

This guide shows how to migrate legacy programmatic database creation code from:

- `new DatabaseConfig()`
- `DatabaseFactory.create(...)`
- old `setXxx(...)` builder-style configuration methods

…to the preferred builder-based style using:

- `Database.builder()`
- fluent `DatabaseBuilder` methods such as `name(...)`, `register(...)`, and `defaultDatabase(...)`
- `DatabaseBuilder.build()`

Use this guide when upgrading older Ebean setup code or when building an automated/semi-automated migration.

---

## Preferred pattern

Prefer code shaped like this:

```java
Database database = Database.builder()
  .name("db")
  .loadFromProperties()
  .dataSourceBuilder(dataSource)
  .register(true)
  .defaultDatabase(true)
  .build();
```

The important points are:

1. Start with `Database.builder()`
2. Configure via `DatabaseBuilder`
3. Finish with `.build()`

---

## Step 1 — Replace `new DatabaseConfig()` with `Database.builder()`

### Before

```java
DatabaseConfig config = new DatabaseConfig();
config.setName("db");
config.loadFromProperties();
```

### After

```java
DatabaseBuilder config = Database.builder()
  .name("db")
  .loadFromProperties();
```

### Notes

- Prefer the `DatabaseBuilder` type for local variables and parameters when possible.
- If existing code only uses standard builder methods, this change is usually mechanical.
- If existing code later reads configuration back, use `config.settings()`.

---

## Step 2 — Replace `DatabaseFactory.create(config)` with `config.build()`

### Before

```java
DatabaseConfig config = new DatabaseConfig();
config.setName("db");
config.loadFromProperties();
Database database = DatabaseFactory.create(config);
```

### After

```java
DatabaseBuilder config = Database.builder()
  .name("db")
  .loadFromProperties();
Database database = config.build();
```

### Short form

```java
Database database = Database.builder()
  .name("db")
  .loadFromProperties()
  .build();
```

---

## Step 3 — Replace `DatabaseFactory.create("name")`

### Before

```java
Database database = DatabaseFactory.create("other");
```

### After

```java
Database database = Database.builder()
  .name("other")
  .loadFromProperties()
  .build();
```

### Important

For **named databases**, set `.name("...")` before `.loadFromProperties()` so the named configuration is loaded.

---

## Step 4 — Replace legacy `setXxx(...)` methods with fluent builder methods

`DatabaseBuilder` already exposes preferred fluent names for most configuration methods.
Use those names when migrating older setup code.

| Legacy call | Preferred call |
|---|---|
| `setName("db")` | `name("db")` |
| `setRegister(false)` | `register(false)` |
| `setDefaultServer(false)` | `defaultDatabase(false)` |
| `setContainerConfig(cfg)` | `containerConfig(cfg)` |
| `setDbSchema("app")` | `dbSchema("app")` |
| `setDataSourceConfig(ds)` | `dataSourceBuilder(ds)` |
| `setReadOnlyDataSourceConfig(ro)` | `readOnlyDataSourceBuilder(ro)` |
| `setRunMigration(true)` | `runMigration(true)` |
| `setDisableClasspathSearch(true)` | `disableClasspathSearch(true)` |
| `setPersistBatch(batch)` | `persistBatch(batch)` |

### Full example

#### Before

```java
DatabaseConfig config = new DatabaseConfig();
config.setName("db");
config.setRegister(false);
config.setDefaultServer(false);
config.setDataSourceConfig(dataSource);
Database database = DatabaseFactory.create(config);
```

#### After

```java
Database database = Database.builder()
  .name("db")
  .register(false)
  .defaultDatabase(false)
  .dataSourceBuilder(dataSource)
  .build();
```

---

## Step 5 — Verify semantics after migration

The migration should preserve behavior, but verify these points:

- `register(true)` is still the default
- `defaultDatabase(true)` is still the default
- call `loadFromProperties()` if the old code loaded configuration from properties
- for named databases, set the name before loading properties
- explicit entity registration via `addClass(...)` / `addAll(...)` is unchanged
- custom datasource wiring via `dataSourceBuilder(...)` and `readOnlyDataSourceBuilder(...)` is unchanged

---

## Manual-review cases

These cases are **not** simple search-and-replace migrations and should be reviewed manually:

### `DatabaseFactory.createWithContextClassLoader(...)`

There is no direct builder shorthand for this today. Keep this as-is for now and migrate the surrounding builder configuration first.

### `DatabaseFactory.initialiseContainer(...)`

This is a container lifecycle concern, not a normal database-builder call. Keep it as-is unless you are intentionally moving the `ContainerConfig` onto the first builder via `containerConfig(...)`.

### `DatabaseFactory.shutdown()`

This is also a lifecycle concern rather than normal builder setup. Leave it alone unless you are making a deliberate lifecycle change.

### Variables or method signatures typed as `DatabaseConfig`

If the code only uses standard builder operations, switch the type to `DatabaseBuilder`.
If the code depends on implementation-specific `DatabaseConfig` methods, review it manually.

### Code that needs read access to builder settings

Use:

```java
DatabaseBuilder builder = Database.builder();
DatabaseBuilder.Settings settings = builder.settings();
```

rather than relying on the concrete `DatabaseConfig` type only to read getters.

---

## Automation notes for AI agents and bulk refactors

This migration is a good candidate for semi-automated upgrading.

### Safe mechanical rewrites

These are usually safe to rewrite automatically:

- `new DatabaseConfig()` → `Database.builder()`
- `DatabaseFactory.create(builder)` → `builder.build()`
- `DatabaseFactory.create("name")` → `Database.builder().name("name").loadFromProperties().build()`
- legacy `setXxx(...)` calls → preferred fluent builder methods

### Flag for manual review

Automatically flag, but do not blindly rewrite:

- `DatabaseFactory.createWithContextClassLoader(...)`
- `DatabaseFactory.initialiseContainer(...)`
- `DatabaseFactory.shutdown()`
- parameters, fields, or return types declared as `DatabaseConfig`
- any use that clearly depends on `DatabaseConfig` implementation details rather than `DatabaseBuilder`

---

## Related guides

- [Database configuration](add-ebean-postgres-database-config.md) — preferred modern setup style using `Database.builder()`
- [Guide index](README.md) — full list of Ebean setup and migration guides
