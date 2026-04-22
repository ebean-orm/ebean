package io.ebean.opentelemetry;

import io.ebeaninternal.server.transaction.ProfileStream;
import io.ebeaninternal.server.transaction.TransactionManager;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * OpenTelemetry implementation of ProfileStream.
 * <p>
 * Holds the transaction span and creates a child span per query/persist event
 * using retrospective timestamps derived from the profiling offsets.
 */
final class OtelProfileStream implements ProfileStream {

  static final AttributeKey<String> DB_SYSTEM = AttributeKey.stringKey("db.system");
  static final AttributeKey<String> DB_OPERATION = AttributeKey.stringKey("db.operation");
  static final AttributeKey<String> EBEAN_BEAN_TYPE = AttributeKey.stringKey("ebean.bean_type");
  static final AttributeKey<Long> EBEAN_ROW_COUNT = AttributeKey.longKey("ebean.row_count");
  static final AttributeKey<String> EBEAN_QUERY_ID = AttributeKey.stringKey("ebean.query_id");
  static final AttributeKey<Long> EBEAN_TOTAL_MICROS = AttributeKey.longKey("ebean.total_micros");

  private final Tracer tracer;
  private final Span txnSpan;
  private final long startNanos;
  private final long startEpochNanos;
  /** True when the transaction span name is still the generic fallback and can be updated. */
  private boolean updateName;

  OtelProfileStream(Tracer tracer, Span txnSpan, boolean updateName) {
    this.tracer = tracer;
    this.txnSpan = txnSpan;
    this.startNanos = System.nanoTime();
    Instant now = Instant.now();
    this.startEpochNanos = now.getEpochSecond() * 1_000_000_000L + now.getNano();
    this.updateName = updateName;
  }

  @Override
  public long offset() {
    return (System.nanoTime() - startNanos) / 1_000L;
  }

  @Override
  public void addQueryEvent(String event, long offset, String beanName, int beanCount, String queryId) {
    long exeMicros = offset() - offset;
    String operation = operationName(event);
    maybeUpdateTxnName(operation, beanName);
    Span child = childSpanBuilder(operation + " " + beanName, offset)
      .setAttribute(DB_OPERATION, operation)
      .setAttribute(EBEAN_BEAN_TYPE, beanName)
      .setAttribute(EBEAN_ROW_COUNT, (long) beanCount)
      .setAttribute(EBEAN_QUERY_ID, queryId)
      .startSpan();
    child.end(startEpochNanos + TimeUnit.MICROSECONDS.toNanos(offset + exeMicros), TimeUnit.NANOSECONDS);
  }

  @Override
  public void addPersistEvent(String event, long offset, String beanName, int beanCount) {
    long exeMicros = offset() - offset;
    String operation = operationName(event);
    maybeUpdateTxnName(operation, beanName);
    Span child = childSpanBuilder(operation + " " + beanName, offset)
      .setAttribute(DB_OPERATION, operation)
      .setAttribute(EBEAN_BEAN_TYPE, beanName)
      .setAttribute(EBEAN_ROW_COUNT, (long) beanCount)
      .startSpan();
    child.end(startEpochNanos + TimeUnit.MICROSECONDS.toNanos(offset + exeMicros), TimeUnit.NANOSECONDS);
  }

  @Override
  public void addEvent(String event, long startOffset) {
    if ("r".equals(event)) {
      txnSpan.setStatus(StatusCode.ERROR, "rollback");
    } else {
      txnSpan.setStatus(StatusCode.OK);
    }
  }

  @Override
  public void end(TransactionManager manager) {
    txnSpan.setAttribute(EBEAN_TOTAL_MICROS, offset());
    txnSpan.end();
  }

  private SpanBuilder childSpanBuilder(String name, long offsetMicros) {
    return tracer.spanBuilder(name)
      .setParent(Context.current().with(txnSpan))
      .setSpanKind(SpanKind.INTERNAL)
      .setAttribute(DB_SYSTEM, "ebean")
      .setStartTimestamp(startEpochNanos + TimeUnit.MICROSECONDS.toNanos(offsetMicros), TimeUnit.NANOSECONDS);
  }

  private void maybeUpdateTxnName(String operation, String beanName) {
    if (updateName) {
      updateName = false;
      txnSpan.updateName(operation + " " + beanName);
    }
  }

  /**
   * Map ebean event codes (from TxnProfileEventCodes) to human-readable operation names.
   */
  static String operationName(String event) {
    switch (event) {
      case "fo":  return "find_one";
      case "fm":  return "find_many";
      case "fe":  return "find_iterate";
      case "fi":  return "find_id_list";
      case "ex":  return "find_exists";
      case "fa":  return "find_attribute";
      case "fas": return "find_attribute_set";
      case "fc":  return "find_count";
      case "fs":  return "find_subquery";
      case "lm":  return "lazy_load_many";
      case "lo":  return "lazy_load_one";
      case "i":   return "insert";
      case "u":   return "update";
      case "d":   return "delete";
      case "ds":  return "delete_soft";
      case "dp":  return "delete_permanent";
      case "uo":  return "orm_update";
      case "uq":  return "update_query";
      case "dq":  return "delete_query";
      case "su":  return "update_sql";
      case "sc":  return "callable_sql";
      default:    return event;
    }
  }
}
