package io.ebean.opentelemetry;

import io.ebean.ProfileLocation;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.SpiProfileHandler;
import io.ebeaninternal.server.transaction.ProfileStream;
import io.ebeaninternal.server.transaction.TransactionProfile;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import org.jspecify.annotations.Nullable;

/**
 * OpenTelemetry implementation of SpiProfileHandler.
 * <p>
 * Creates a transaction span as a child of the currently active OpenTelemetry
 * span. If no active span exists on the current thread, no profiling stream is
 * created (returns null) to avoid generating noisy root-level spans.
 * <p>
 * Register via ServiceLoader: add this class to
 * {@code META-INF/services/io.ebeaninternal.api.SpiProfileHandler}.
 */
public final class OtelProfileHandler implements SpiProfileHandler, Plugin {

  static final String INSTRUMENTATION_NAME = "io.ebean";

  private Tracer tracer;

  public OtelProfileHandler() {
    // tracer resolved lazily in configure() once the OTel SDK is initialized
  }

  /** For testing: inject tracer directly rather than using GlobalOpenTelemetry. */
  OtelProfileHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public void configure(SpiServer server) {
    if (this.tracer == null) {
      this.tracer = GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME);
    }
  }

  @Override
  public void online(boolean online) {
    // nothing to do
  }

  @Override
  public void shutdown() {
    // nothing to do — OTel SDK lifecycle is managed by the application
  }

  /**
   * Create a ProfileStream for this transaction, or return null if there is no
   * active OpenTelemetry span on the current thread.
   *
   * @param location the profile location for explicit {@code @Transactional} methods,
   *                 or null for implicit read-only transactions
   */
  @Override
  public @Nullable ProfileStream createProfileStream(@Nullable ProfileLocation location) {
    if (!Span.current().getSpanContext().isValid()) {
      // No active OTel trace context — don't create spans to avoid noise
      return null;
    }
    String spanName;
    boolean updateName;
    if (location != null) {
      spanName = location.label();
      updateName = false;
    } else {
      // Implicit read-only transaction: name will be refined on the first query event
      spanName = "ebean.transaction";
      updateName = true;
    }
    Span txnSpan = tracer.spanBuilder(spanName)
      .setSpanKind(SpanKind.INTERNAL)
      .setAttribute(OtelProfileStream.DB_SYSTEM, "ebean")
      .startSpan();
    return new OtelProfileStream(tracer, txnSpan, updateName);
  }

  /**
   * The stream handles span lifecycle inline — nothing to do here.
   */
  @Override
  public void collectTransactionProfile(TransactionProfile transactionProfile) {
    // no-op: OtelProfileStream.end() already closed the span
  }
}
