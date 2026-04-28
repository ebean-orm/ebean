package io.ebean.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OtelProfileHandler and OtelProfileStream.
 */
class OtelProfileHandlerTest {

  /** Simple in-memory span collector for assertions. */
  static final class CapturingExporter implements SpanExporter {

    final List<SpanData> spans = new ArrayList<>();

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      this.spans.addAll(spans);
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }

  }

  private CapturingExporter exporter;
  private SdkTracerProvider tracerProvider;
  private Tracer tracer;
  private OtelProfileHandler handler;

  @BeforeEach
  void setup() {
    exporter = new CapturingExporter();
    tracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
      .build();
    tracer = tracerProvider.get("test");
    handler = new OtelProfileHandler(tracer);
  }

  @AfterEach
  void tearDown() {
    tracerProvider.close();
  }

  // ----------------------------------------------------------
  // createProfileStream
  // ----------------------------------------------------------

  @Test
  void createProfileStream_noActiveContext_returnsNull() {
    // No span active on thread — should not create a stream
    assertNull(handler.createProfileStream(null));
    assertNull(handler.createProfileStream(mockLocation("SomeService.doWork")));
  }

  @Test
  void createProfileStream_withActiveContext_returnsStream() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      var stream = handler.createProfileStream(null);
      assertNotNull(stream);
      stream.addEvent("c", 0); // commit
      stream.end(null);
    } finally {
      parent.end();
    }
  }

  @Test
  void createProfileStream_withLocation_usesLabelAsSpanName() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      var stream = handler.createProfileStream(mockLocation("OrderService.placeOrder"));
      assertNotNull(stream);
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    // parent + txn span
    SpanData txnSpan = findSpan("OrderService.placeOrder");
    assertNotNull(txnSpan, "Expected span named 'OrderService.placeOrder'");
    assertEquals("ebean", txnSpan.getAttributes().get(OtelProfileStream.DB_SYSTEM));
  }

  // ----------------------------------------------------------
  // Transaction span name update for implicit transactions
  // ----------------------------------------------------------

  @Test
  void implicitTransaction_nameUpdatedOnFirstQueryEvent() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(null);
      assertNotNull(stream);
      // First query event should update the transaction span name
      stream.addQueryEvent("fm", stream.offset(), "Customer", 5, "qplan-1", "select ...");
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    SpanData txnSpan = findSpan("find_many Customer");
    assertNotNull(txnSpan, "Expected txn span renamed to 'find_many Customer'");
  }

  // ----------------------------------------------------------
  // Query events → child spans
  // ----------------------------------------------------------

  @Test
  void addQueryEvent_createsChildSpanWithAttributes() {
    Span parent = tracer.spanBuilder("http.get /orders").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(mockLocation("OrderService.findAll"));
      assertNotNull(stream);
      long offset = stream.offset();
      // Simulate some time passing then record a find_many
      stream.addQueryEvent("fm", offset, "Order", 42, "plan-abc", "select ...");
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }

    SpanData querySpan = findSpan("find_many Order");
    assertNotNull(querySpan, "Expected child span 'find_many Order'");
    assertEquals("ebean", querySpan.getAttributes().get(OtelProfileStream.DB_SYSTEM));
    assertEquals("find_many", querySpan.getAttributes().get(OtelProfileStream.DB_OPERATION));
    assertEquals("Order", querySpan.getAttributes().get(OtelProfileStream.EBEAN_BEAN_TYPE));
    assertEquals(42L, querySpan.getAttributes().get(OtelProfileStream.EBEAN_ROW_COUNT));
    assertEquals("plan-abc", querySpan.getAttributes().get(OtelProfileStream.EBEAN_QUERY_ID));
  }

  @Test
  void addQueryEvent_multipleEvents_eachCreatesChildSpan() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(mockLocation("MyService.doAll"));
      assertNotNull(stream);
      stream.addQueryEvent("fo", stream.offset(), "User", 1, "p1", "select from user");
      stream.addQueryEvent("fm", stream.offset(), "Order", 10, "p2", "select from order");
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    assertNotNull(findSpan("find_one User"));
    assertNotNull(findSpan("find_many Order"));
  }

  // ----------------------------------------------------------
  // Persist events → child spans
  // ----------------------------------------------------------

  @Test
  void addPersistEvent_createsChildSpanWithAttributes() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(mockLocation("CartService.checkout"));
      assertNotNull(stream);
      long offset = stream.offset();
      stream.addPersistEvent("i", offset, "Order", 1);
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    SpanData persistSpan = findSpan("insert Order");
    assertNotNull(persistSpan, "Expected child span 'insert Order'");
    assertEquals("insert", persistSpan.getAttributes().get(OtelProfileStream.DB_OPERATION));
    assertEquals("Order", persistSpan.getAttributes().get(OtelProfileStream.EBEAN_BEAN_TYPE));
    assertEquals(1L, persistSpan.getAttributes().get(OtelProfileStream.EBEAN_ROW_COUNT));
  }

  // ----------------------------------------------------------
  // Commit / Rollback → span status
  // ----------------------------------------------------------

  @Test
  void addEvent_commit_setsStatusOk() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(mockLocation("Svc.ok"));
      assertNotNull(stream);
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    SpanData txnSpan = findSpan("Svc.ok");
    assertNotNull(txnSpan);
    assertEquals(StatusCode.OK, txnSpan.getStatus().getStatusCode());
  }

  @Test
  void addEvent_rollback_setsStatusError() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(mockLocation("Svc.fail"));
      assertNotNull(stream);
      stream.addEvent("r", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    SpanData txnSpan = findSpan("Svc.fail");
    assertNotNull(txnSpan);
    assertEquals(StatusCode.ERROR, txnSpan.getStatus().getStatusCode());
  }

  // ----------------------------------------------------------
  // end() — total_micros attribute
  // ----------------------------------------------------------

  @Test
  void end_setsTotalMicrosAttribute() {
    Span parent = tracer.spanBuilder("parent").startSpan();
    try (Scope ignored = parent.makeCurrent()) {
      OtelProfileStream stream = (OtelProfileStream) handler.createProfileStream(mockLocation("Svc.timed"));
      assertNotNull(stream);
      stream.addEvent("c", 0);
      stream.end(null);
    } finally {
      parent.end();
    }
    SpanData txnSpan = findSpan("Svc.timed");
    assertNotNull(txnSpan);
    Long totalMicros = txnSpan.getAttributes().get(OtelProfileStream.EBEAN_TOTAL_MICROS);
    assertNotNull(totalMicros);
    assertTrue(totalMicros >= 0, "total_micros should be non-negative");
  }

  // ----------------------------------------------------------
  // operationName mapping
  // ----------------------------------------------------------

  @Test
  void operationName_allEventCodes() {
    assertEquals("find_one", OtelProfileStream.operationName("fo"));
    assertEquals("find_many", OtelProfileStream.operationName("fm"));
    assertEquals("find_iterate", OtelProfileStream.operationName("fe"));
    assertEquals("find_id_list", OtelProfileStream.operationName("fi"));
    assertEquals("find_exists", OtelProfileStream.operationName("ex"));
    assertEquals("find_attribute", OtelProfileStream.operationName("fa"));
    assertEquals("find_attribute_set", OtelProfileStream.operationName("fas"));
    assertEquals("find_count", OtelProfileStream.operationName("fc"));
    assertEquals("find_subquery", OtelProfileStream.operationName("fs"));
    assertEquals("lazy_load_many", OtelProfileStream.operationName("lm"));
    assertEquals("lazy_load_one", OtelProfileStream.operationName("lo"));
    assertEquals("insert", OtelProfileStream.operationName("i"));
    assertEquals("update", OtelProfileStream.operationName("u"));
    assertEquals("delete", OtelProfileStream.operationName("d"));
    assertEquals("delete_soft", OtelProfileStream.operationName("ds"));
    assertEquals("delete_permanent", OtelProfileStream.operationName("dp"));
    assertEquals("orm_update", OtelProfileStream.operationName("uo"));
    assertEquals("update_query", OtelProfileStream.operationName("uq"));
    assertEquals("delete_query", OtelProfileStream.operationName("dq"));
    assertEquals("update_sql", OtelProfileStream.operationName("su"));
    assertEquals("callable_sql", OtelProfileStream.operationName("sc"));
    // Unknown code returned as-is
    assertEquals("xyz", OtelProfileStream.operationName("xyz"));
  }

  // ----------------------------------------------------------
  // Helpers
  // ----------------------------------------------------------

  private SpanData findSpan(String name) {
    return exporter.spans.stream()
      .filter(s -> name.equals(s.getName()))
      .findFirst()
      .orElse(null);
  }

  private io.ebean.ProfileLocation mockLocation(String label) {
    return new io.ebean.ProfileLocation() {
      @Override public boolean obtain() { return false; }
      @Override public String location() { return label; }
      @Override public String label() { return label; }
      @Override public String fullLocation() { return label; }
      @Override public void add(long executionTime) {}
      @Override public boolean trace() { return true; }
      @Override public void setTraceCount(int traceCount) {}
    };
  }
}
