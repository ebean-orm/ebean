package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiTxnLogger;

import java.util.concurrent.atomic.AtomicLong;

public final class DLogManager implements SpiLogManager {

  private final SpiLogger sql;
  private final SpiLogger summary;
  private final SpiLogger txn;
  private final boolean useIds;
  private final AtomicLong counter = new AtomicLong(1000);
  private final DTxnLogger readOnly;

  public DLogManager(SpiLogger sql, SpiLogger summary, SpiLogger txn) {
    this.sql = sql;
    this.summary = summary;
    this.txn = txn;
    this.useIds = txn.isDebug();
    this.readOnly = new DTxnLogger(null, sql, summary, txn);
  }

  @Override
  public boolean enableBindLog() {
    return sql.isDebug();
  }

  @Override
  public SpiLogger sql() {
    return sql;
  }

  @Override
  public SpiTxnLogger logger() {
    String id = useIds ? Long.toString(counter.incrementAndGet()) : "";
    return new DTxnLogger(id, sql, summary, txn);
  }

  @Override
  public SpiTxnLogger readOnlyLogger() {
    return readOnly;
  }
}
