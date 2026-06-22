# Guide: Migrate JSON APIs from Jackson core to avaje-json-core

## Purpose

This guide covers the one-step cutover in Ebean from Jackson core JSON APIs to
avaje-json-core APIs.

Use this when upgrading code that references:

- `com.fasterxml.jackson.core.JsonParser`
- `com.fasterxml.jackson.core.JsonGenerator`
- `com.fasterxml.jackson.core.JsonFactory`

The replacement types are:

- `io.avaje.json.JsonReader`
- `io.avaje.json.JsonWriter`
- `io.avaje.json.stream.JsonStream`

---

## Breaking API changes

| Previous API | New API |
|---|---|
| `JsonParser` | `JsonReader` |
| `JsonGenerator` | `JsonWriter` |
| `JsonFactory` | `JsonStream` |
| `DatabaseBuilder.jsonFactory(...)` | `DatabaseBuilder.jsonStream(...)` |
| `DatabaseConfig.getJsonFactory()/setJsonFactory(...)` | `DatabaseConfig.getJsonStream()/setJsonStream(...)` |

---

## Typical migration rewrites

### Parser and generator signatures

```java
// before
void read(JsonParser parser)
void write(JsonGenerator generator)

// after
void read(JsonReader parser)
void write(JsonWriter generator)
```

### Database configuration

```java
// before
Database.builder().jsonFactory(factory)

// after
Database.builder().jsonStream(stream)
```

### JSON utility calls

`EJson` and `JsonContext` APIs now operate on `JsonReader` and `JsonWriter` types.
If your code was calling those APIs with Jackson core types, switch to avaje types.

---

## Dependency and module notes

- `ebean-core` no longer requires a direct `jackson-core` dependency for JSON
  parsing/writing.
- `jackson-databind` remains optional for `ObjectMapper` compatibility paths.
- `ebean-jackson-mapper` remains the compatibility bridge module for mapper-based
  integrations.

---

## Behavior notes to verify during upgrade

1. Parser token handling is now based on avaje `JsonReader.Token`.
2. Scalar JSON reads (for example booleans, date-time, array scalar types) should
   be validated in your tests if you previously depended on Jackson token quirks.
3. If your integration uses transient assoc-many JSON mapping with ObjectMapper,
   keep ObjectMapper wiring enabled.

---

## Validation checklist

1. Compile all modules that implement or consume `io.ebean.text.json` APIs.
2. Run module tests that cover JSON scalar conversion and bean JSON round-trips.
3. Confirm no remaining `com.fasterxml.jackson.core.*` imports in migrated code.
4. Keep `ObjectMapper` compatibility tests if your project depends on mapper paths.

