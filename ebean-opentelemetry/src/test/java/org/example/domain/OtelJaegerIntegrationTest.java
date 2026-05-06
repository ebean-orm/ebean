package org.example.domain;

import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.config.ProfilingConfig;
import io.ebean.datasource.DataSourceBuilder;
import io.ebean.datasource.DataSourcePool;
import io.ebean.opentelemetry.OtelProfileHandler;
import io.ebeaninternal.api.SpiProfileHandler;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.example.domain.query.QOtelOrder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <pre>
 * docker run --rm --name jaeger \
 *    -e COLLECTOR_OTLP_ENABLED=true \
 *    -p 4317:4317 \
 *    -p 16686:16686 \
 *    jaegertracing/all-in-one:1.62.0
 *
 * Jaeger UI will be at http://localhost:16686, and OTLP gRPC ingest at http://localhost:4317.
 * </pre>
 */
class OtelJaegerIntegrationTest {

  private static final String SERVICE_NAME = "ebean-otel-it";

  @Disabled
  @Test
  void otlpExport_visibleInJaeger_fromRealEbeanQueryFlow() {

    Resource resource = Resource.getDefault().merge(Resource.create(
      Attributes.of(AttributeKey.stringKey("service.name"), SERVICE_NAME)));

    OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
      .setEndpoint("http://localhost:4317")
      .setTimeout(Duration.ofSeconds(5))
      .build();

    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
      .setResource(resource)
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
      .build();

    try {
      Tracer tracer = tracerProvider.get("it");
      Database db = createDatabase(tracer);
      try {

        Span parent = tracer.spanBuilder("parent").startSpan();
        try (Scope ignored = parent.makeCurrent()) {
          doStuff(db);
          doStuff(db);
          doStuff(db);
          doStuff(db);

        } finally {
          parent.end();
        }
      } finally {
        db.shutdown();
      }

      tracerProvider.forceFlush().join(5, TimeUnit.SECONDS);

    } finally {
      tracerProvider.close();
    }
  }

  private void doStuff(Database db) {
    var first = exercise(db);

    var found = new QOtelOrder()
      .id.eq(first.getId())
      .findOne();

    assertThat(found).isNotNull();
  }

  // @Transactional
  private OtelOrder exercise(Database db) {
    var first = insertSome(db);

    List<OtelOrder> rows = db.find(OtelOrder.class)
      .setLabel("findNewOrders")
      .where()
      .eq("status", "NEW")
      .findList();

    assertThat(rows).isNotEmpty();
    return first;
  }

  private OtelOrder insertSome(Database db) {
    try (Transaction transaction = db.beginTransaction()) {
      transaction.setLabel("saveNewOrders");
      var first = new OtelOrder("NEW");
      db.save(first);
      db.save(new OtelOrder("NEW"));
      db.save(new OtelOrder("NEW"));
      db.save(new OtelOrder("NEW"));
      db.save(new OtelOrder("NEW"));
      transaction.commit();
      return first;
    }
  }

  private Database createDatabase(Tracer tracer) {
    ProfilingConfig profilingConfig = new ProfilingConfig();
    profilingConfig.setEnabled(true);

    DataSourcePool ds = DataSourceBuilder.create()
      .url("jdbc:h2:mem:otelit;DB_CLOSE_DELAY=-1")
      .username("sa")
      .password("")
      .driver("org.h2.Driver")
      .build();

    return Database.builder()
      .name("otel_h2")
      .defaultDatabase(true)
      .register(true)
      .loadFromProperties()
      .ddlGenerate(true)
      .ddlRun(true)
      .profilingConfig(profilingConfig)
      .putServiceObject(SpiProfileHandler.class, new OtelProfileHandler(tracer))
      .addClass(OtelOrder.class)
      .dataSource(ds)
      .build();
  }
}
