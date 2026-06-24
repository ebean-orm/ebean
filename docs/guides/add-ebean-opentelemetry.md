# Guide: Add Ebean OpenTelemetry tracing

## Purpose

This guide explains how to enable Ebean transaction tracing with OpenTelemetry and,
most importantly, how to order startup so Ebean sees the intended global
OpenTelemetry instance.

Use this guide when adding `ebean-opentelemetry`, diagnosing missing Ebean spans,
or fixing `GlobalOpenTelemetry` double-registration errors.

---

## Overview

`ebean-opentelemetry` provides an Ebean profiling handler that creates transaction
spans as children of the current active OpenTelemetry span. It does not create
top-level request, job, or Lambda invocation spans by itself.

The handler resolves its tracer from `GlobalOpenTelemetry` when the Ebean
`Database` is configured. For that reason, the application must build and register
the OpenTelemetry SDK before any Ebean `Database` beans are created.

Rules of thumb:

- Register the global OpenTelemetry instance once.
- Register it before building Ebean databases.
- Model that ordering as a real DI dependency.
- Do not call `GlobalOpenTelemetry.set(...)` or `buildAndRegisterGlobal()` in
  multiple places.

---

## Step 1 - Add the dependency

```xml
<dependency>
  <groupId>io.ebean</groupId>
  <artifactId>ebean-opentelemetry</artifactId>
  <version>${ebean.version}</version>
</dependency>
```

The module registers the Ebean OpenTelemetry profile handler via `ServiceLoader`.
No manual Ebean plugin registration is normally required.

---

## Step 2 - Build OpenTelemetry before Ebean databases

Create one application-owned OpenTelemetry bean. For example, when using
`avaje-metrics-otel`:

```java
import io.avaje.config.Configuration;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.metrics.otel.MetricsOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;

import java.time.Duration;

@Factory
class OpenTelemetryConfig {

  @Bean
  OpenTelemetry openTelemetry(Configuration config) {
    return MetricsOpenTelemetry.builder()
      .endpoint(config.get("otel.endpoint"))
      .serviceName(config.get("otel.serviceName", "orders"))
      .deploymentEnvironmentName(config.get("app.env", "local"))
      .meterInterval(Duration.ofSeconds(30))
      .traceInterval(Duration.ofSeconds(30))
      .buildAndRegisterGlobal();
  }
}
```

If you build the SDK directly, use the same principle: create the SDK once and
register that instance globally before any Ebean databases are built.

---

## Step 3 - Make database beans depend on OpenTelemetry

In DI code, make the `Database` bean method accept `OpenTelemetry`. This parameter
is intentionally present to make startup order deterministic: OpenTelemetry is
created and registered before Ebean configures the database and profile handler.

```java
import io.avaje.config.Configuration;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.ebean.Database;
import io.ebean.datasource.DataSourceBuilder;
import io.opentelemetry.api.OpenTelemetry;

@Factory
class DatabaseConfig {

  @Bean
  Database database(OpenTelemetry openTelemetry, Configuration config) {
    var dataSource = DataSourceBuilder.create()
      .url(config.get("db.url"))
      .username(config.get("db.username"))
      .password(config.get("db.password"));

    return Database.builder()
      .name("db")
      .dataSourceBuilder(dataSource)
      .build();
  }
}
```

For Spring, use the same dependency shape: either inject `OpenTelemetry` into the
database `@Bean` method or use `@DependsOn` to ensure the OpenTelemetry bean is
initialized first.

Do not invert the dependency by making OpenTelemetry depend on the Ebean
`Database`. That creates a startup cycle and can still initialize Ebean before the
global OpenTelemetry instance is ready.

---

## Step 4 - Create a parent span at the application boundary

Ebean transaction spans are child spans. They are only created when a recording
OpenTelemetry span is active on the current thread.

Use HTTP server instrumentation, Lambda instrumentation, or an application-level
root span around the top-level request/job boundary. Ebean will then attach
transaction spans beneath that current span.

---

## Troubleshooting

### `GlobalOpenTelemetry.set has already been called`

This usually means more than one component is trying to register a global SDK, or
some startup path touched the global before the application registered its SDK.

Fixes:

1. Keep exactly one `buildAndRegisterGlobal()` / `GlobalOpenTelemetry.set(...)`
   call in the application.
2. Build that OpenTelemetry bean before Ebean `Database` beans.
3. Remove duplicate OTEL setup from tests, helper factories, or secondary modules.

### No Ebean spans appear

Check:

1. `ebean-opentelemetry` is on the runtime classpath.
2. OpenTelemetry is registered before Ebean databases are built.
3. There is a current recording parent span when Ebean transactions run.
4. Sampling is not dropping the parent trace.
