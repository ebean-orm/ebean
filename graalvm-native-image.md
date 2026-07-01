# GraalVM Native Image

Ebean ORM supports GraalVM native image compilation. No additional dependencies or
native-image configuration files are required beyond what Ebean already provides.

## How it works

Ebean uses **bytecode enhancement at compile time** (via the ebean-maven-plugin or
ebean-gradle-plugin). The enhanced entity classes are compiled directly into the native
image. Because the enhancement happens ahead-of-time, Ebean does not rely on runtime
reflection for normal ORM operations — queries, inserts, updates, deletes, and
associations all work without additional configuration.

Entity classes do require reflection registration for native image, but this is handled
**automatically by the querybean-generator** annotation processor at compile time.
Projects using the querybean-generator (which is the standard and expected setup) have
nothing extra to do for `@Entity` classes.

## Setup

Ensure the ebean enhancement plugin runs during your build, as it normally would for
a standard JVM application. No extra steps are required for native image.

**Maven:**
```xml
<plugin>
  <groupId>io.ebean</groupId>
  <artifactId>ebean-maven-plugin</artifactId>
  <version>${ebean.version}</version>
  <extensions>true</extensions>
</plugin>
```

**Gradle:**
```kotlin
plugins {
  id("io.ebean") version "${ebeanVersion}"
}
```

## DtoQuery and reflection

[`DtoQuery`](https://ebean.io/docs/query/dto) maps SQL results onto plain classes using
their public constructors and setter methods. This mapping uses reflection at runtime, so
DTO classes must be explicitly registered for reflection in your native-image configuration.

Create `src/main/resources/META-INF/native-image/<group-id>/<artifact-id>/reflect-config.json`
and add an entry for each DTO class:

```json
[
  {
    "name": "com.example.CustomerDto",
    "allDeclaredConstructors": true,
    "allPublicMethods": true
  },
  {
    "name": "com.example.OrderSummaryDto",
    "allDeclaredConstructors": true,
    "allPublicMethods": true
  }
]
```

> **Note:** `@Entity` classes are registered automatically by the querybean-generator —
> no manual entries are needed for them.
