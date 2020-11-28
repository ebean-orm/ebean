package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogManager;
import io.ebeaninternal.api.SpiLogger;

public class DLogManager implements SpiLogManager {

  private final SpiLogger sql;
  private final SpiLogger summary;
  private final SpiLogger txn;

  public DLogManager(SpiLogger sql, SpiLogger summary, SpiLogger txn) {
    this.sql = sql;
    this.summary = summary;
    this.txn = txn;
  }

  @Override
  public SpiLogger txn() {
    return txn;
  }

  @Override
  public SpiLogger sql() {
    return sql;
  }

  @Override
  public SpiLogger sum() {
    return summary;
  }

}
