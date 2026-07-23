# Guide: `@DbJson` / `@DbJsonB` mapping support

## Purpose

Ebean can map `@DbJson` and `@DbJsonB` properties in three ways:

- **Built-in** JSON support, backed by **avaje-json-core** — no extra dependency.
- **Jackson `ObjectMapper`**, provided by **`ebean-jackson-mapper`**.
- **Avaje Jsonb**, provided by **`ebean-avajejsonb-mapper`** and its generated adapters.

The built-in mapper handles the common natural JSON types. Add one mapper module for
typed collections, POJOs, records, and other custom types.

> If a property type is **not** handled built-in and no mapper module is on the classpath,
> Ebean fails fast at startup:
>
> ```text
> Unsupported @DbJson mapping - missing JSON mapper dependency for <property>
> ```

---

## Quick reference

| Property type | Built-in (avaje-json-core) | Mapper module |
|---|:---:|---|
| `String` | ✅ | |
| `List<String>`, `List<Long>` | ✅ | |
| `Set<String>`, `Set<Long>` | ✅ | |
| `Map<String, Object>`, `Map<String, ?>` | ✅ | |
| `Map<String, String>` | ✅ | |
| `Map<Enum, Object>`, `Map<Enum, String>` | ✅ | |
| `List`/`Set` of any other element type (`Integer`, `Double`, `UUID`, `LocalDate`, an enum, a POJO, …) | | Jackson or Avaje Jsonb |
| `Map` with a typed value other than `String`/`Object` (`Map<String,Integer>`, `Map<String,UUID>`, …) | | Jackson or Avaje Jsonb |
| `Map` with a key other than `String` or an enum (`Map<Integer, …>`, `Map<UUID, …>`) | | Jackson or Avaje Jsonb |
| POJOs, records, or any other type | | Jackson or Avaje Jsonb |

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

## Jackson `ObjectMapper`

`ebean-jackson-mapper` uses a Jackson `ObjectMapper` for the property types not handled
built-in:

- **Typed collections** — `List`/`Set` whose element type is not `String` or `Long`
  (for example `List<Integer>`, `List<UUID>`, `List<LocalDate>`, `List<MyEnum>`, `List<MyPojo>`).
- **Typed-value maps** — a `Map` value type other than `String`/`Object`
  (for example `Map<String,Integer>`, `Map<String,UUID>`, `Map<String,MyPojo>`).
- **Non-`String`/non-enum map keys** — for example `Map<Integer,Object>`, `Map<UUID,String>`.
- **POJOs, records, and any other custom type.**

> **Jackson marker annotation override:** if the **field or getter** carries a Jackson annotation
> (anything meta-annotated with `com.fasterxml.jackson.annotation.JacksonAnnotation`), Ebean
> uses the `ObjectMapper` path even when the type would otherwise be handled built-in.

---

### Adding `ebean-jackson-mapper`

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

## Avaje Jsonb

`ebean-avajejsonb-mapper` uses Avaje Jsonb adapters. Annotate each JSON payload type with
`@Json`, or use `@Json.Import`, and configure `avaje-jsonb-generator` as an annotation
processor.

```xml
<dependency>
  <groupId>io.ebean</groupId>
  <artifactId>ebean-avajejsonb-mapper</artifactId>
  <version>${ebean.version}</version>
</dependency>
```

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>io.avaje</groupId>
        <artifactId>avaje-jsonb-generator</artifactId>
        <version>${avaje-jsonb.version}</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

For example:

```java
import io.avaje.jsonb.Json;

@Json
public class Address {
  public String line1;
  public String city;
}
```

Avaje Jsonb preserves a property's declared generic type, so `List<Address>` and other
parameterised JSON values use the generated `Address` adapter.

### Avaje JsonNode

`@DbJson` and `@DbJsonB` properties declared as `io.avaje.json.node.JsonNode` are supported
when the application includes `avaje-json-node`. Its Jsonb component supplies the JSON tree
adapters; no application-generated adapter is needed for the node hierarchy.

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-json-node</artifactId>
  <version>${avaje-jsonb.version}</version>
</dependency>
```

## Mapper module selection

Use exactly one mapper module in an application: `ebean-jackson-mapper` or
`ebean-avajejsonb-mapper`. Ebean selects a single `ScalarJsonMapper` service provider, so
having both modules on the runtime classpath is not supported.

The `ebean` composite dependency includes `ebean-jackson-mapper`. Applications using Avaje
Jsonb should depend on the individual Ebean modules they need instead of that composite.

To migrate from Jackson to Avaje Jsonb, remove `ebean-jackson-mapper`, add
`ebean-avajejsonb-mapper`, and generate adapters for each JSON payload type.

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
  only produces natural JSON types — for example a JSON number always parses to `Long`.

---

## Choosing

- Prefer the **built-in** mappings for the common cases (`String`, string/long lists and sets,
  object/string maps) to avoid an additional mapper module.
- Add **`ebean-jackson-mapper`** or **`ebean-avajejsonb-mapper`** when you need rich POJO JSON
  columns or typed collections / typed-value maps.
