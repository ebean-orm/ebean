package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiTxnLogger;
import io.ebeaninternal.server.util.Str;

final class DTxnLogger implements SpiTxnLogger {

  private final String id;
  private final String logPrefix;
  private final SpiLogger sql;
  private final SpiLogger sum;
  private final SpiLogger txn;

  DTxnLogger(String id, SpiLogger sql, SpiLogger sum, SpiLogger txn) {
    this.id = id;
    this.logPrefix = id == null ? "" : "txn[" + id + "] ";
    this.sql = sql;
    this.sum = sum;
    this.txn = txn;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public boolean isLogSql() {
    return sql.isDebug();
  }

  @Override
  public boolean isLogSummary() {
    return sum.isDebug();
  }

  @Override
  public void sql(String[] msg) {
    sql.debug(Str.add(logPrefix, msg));
  }

  @Override
  public void sum(String[] msg) {
    sum.debug(Str.add(logPrefix, msg));
  }

  @Override
  public void txn(String[] msg) {
    txn.debug(Str.add(logPrefix, msg));
  }

  @Override
  public void notifyCommit() {
    txn.debug(Str.add(logPrefix, "Commit"));
  }

  @Override
  public void notifyQueryOnly() {
    // do nothing
  }

  @Override
  public void notifyRollback(Throwable cause) {
    if (txn.isDebug()) {
      String msg = logPrefix + "Rollback";
      if (cause != null) {
        msg += " error: " + formatThrowable(cause);
      }
      txn.debug(msg);
    }
  }

  private String formatThrowable(Throwable e) {
    if (e == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    formatThrowable(e, sb);
    return sb.toString();
  }

  private void formatThrowable(Throwable e, StringBuilder sb) {
    sb.append(e.toString());
    StackTraceElement[] stackTrace = e.getStackTrace();
    if (stackTrace.length > 0) {
      sb.append(" stack0: ");
      sb.append(stackTrace[0]);
    }
    Throwable cause = e.getCause();
    if (cause != null) {
      sb.append(" cause: ");
      formatThrowable(cause, sb);
    }
  }
}
