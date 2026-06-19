# Guide: `@DbJson` / `@DbJsonB` mapping support — built-in vs Jackson ObjectMapper

## Purpose

Ebean can map `@DbJson` and `@DbJsonB` properties in two ways:

- **Built-in** JSON support, backed by **avaje-json-core** — no extra dependency.
- **Jackson `ObjectMapper`**, provided by the **`ebean-jackson-mapper`** module — used
  for everything the built-in support does not handle.

This guide lists exactly which property types are handled built-in and which require
`ebean-jackson-mapper`.

> If a property type is **not** handled built-in and `ebean-jackson-mapper` is not on the
> classpath, Ebean fails fast at startup:
>
> ```text
> Unsupported @DbJson mapping - Missing dependency ebean-jackson-mapper?
> Jackson ObjectMapper not present for <property>
> ```

---

## Quick reference

| Property type | Built-in (avaje-json-core) | Needs `ebean-jackson-mapper` |
|---|:---:|:---:|
| `String` | ✅ | |
| `List<String>`, `List<Long>` | ✅ | |
| `Set<String>`, `Set<Long>` | ✅ | |
| `Map<String, Object>`, `Map<String, ?>` | ✅ | |
| `Map<String, String>` | ✅ | |
| `Map<Enum, Object>`, `Map<Enum, String>` | ✅ | |
| `List`/`Set` of any other element type (`Integer`, `Double`, `UUID`, `LocalDate`, an enum, a POJO, …) | | ✅ |
| `Map` with a typed value other than `String`/`Object` (`Map<String,Integer>`, `Map<String,UUID>`, …) | | ✅ |
| `Map` with a key other than `String` or an enum (`Map<Integer, …>`, `Map<UUID, …>`) | | ✅ |
| POJOs, records, or any other type | | ✅ |

---

## Built-in support (no Jackson required)

The built-in path materialises JSON into the *natural* JSON value types
(`String`, `Long`, `BigDecimal`, `Boolean`, `Map`, `List`). It is therefore type-safe only
for the following declared property types:

- **`String`** — stored as raw JSON text.
- **`List<String>`** and **`List<Long>`**.
- **`Set<String>`** and **`Set<Long>`**.
- **`Map<K, V>`** where:
  - the key `K` is `String` or an **enum**, and
  - the value `V` is `Object`, `String`, or a wildcard `?`.

  So `Map<String,Object>`, `Map<String,String>`, `Map<Enum,Object>` and `Map<Enum,String>`
  are all built-in.

These mappings work across all supported storage types — `VARCHAR`, `CLOB`, `BLOB`, and
Postgres `json` / `jsonb` — without `ebean-jackson-mapper`.

---

## Everything else → Jackson `ObjectMapper`

Any other `@DbJson` / `@DbJsonB` property routes to the Jackson `ObjectMapper` path, which
requires `ebean-jackson-mapper`:

- **Typed collections** — `List`/`Set` whose element type is not `String` or `Long`
  (for example `List<Integer>`, `List<UUID>`, `List<LocalDate>`, `List<MyEnum>`, `List<MyPojo>`).
- **Typed-value maps** — a `Map` value type other than `String`/`Object`
  (for example `Map<String,Integer>`, `Map<String,UUID>`, `Map<String,MyPojo>`).
- **Non-`String`/non-enum map keys** — for example `Map<Integer,Object>`, `Map<UUID,String>`.
- **POJOs, records, and any other custom type.**

> **Jackson marker annotation override:** if the property type carries a Jackson annotation
> (anything meta-annotated with `com.fasterxml.jackson.annotation.JacksonAnnotation`), Ebean
> uses the `ObjectMapper` path even when the type would otherwise be handled built-in.

---

## Adding `ebean-jackson-mapper`

```xml
<dependency>
  <groupId>io.ebean</groupId>
  <artifactId>ebean-jackson-mapper</artifactId>
  <version>${ebean.version}</version>
</dependency>
```

A Jackson `ObjectMapper` must be available (via `jackson-databind`). Ebean detects it and
registers the mapper-based JSON support automatically.

---

## Notes

- **Enum map keys** are serialised using the enum `name()` (for example `ACTIVE`), not any
  `@DbEnumValue` mapping. Round-trips are correct; the DB value mapping is not applied to
  JSON keys.
- **`@DbArray` alternative:** for typed *scalar* collections (`List`/`Set` of `Integer`,
  `Long`, `UUID`, `Double`, an enum, …) consider `@DbArray`, which maps to a native DB array
  (with a JSON fallback on platforms without array support) and supports more element types
  than built-in `@DbJson` collections.
- The reason typed value/element collections need a real mapper is that the built-in path
  only produces natural JSON types — for example a JSON number always parses to `Long`, so a
  declared `List<Integer>` or `Map<String,Integer>` could not be populated safely without a
  type-aware mapper.

---

## Choosing

- Prefer the **built-in** mappings for the common cases (`String`, string/long lists and sets,
  object/string maps) to avoid pulling in Jackson.
- Add **`ebean-jackson-mapper`** when you need rich POJO JSON columns or typed collections /
  typed-value maps.
