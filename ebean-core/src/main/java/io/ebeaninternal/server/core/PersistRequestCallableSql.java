package io.ebeaninternal.server.core;

import io.ebean.CallableSql;
import io.ebeaninternal.api.*;
import io.ebeaninternal.api.BindParams.Param;
import io.ebeaninternal.server.persist.PersistExecute;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Persist request specifically for CallableSql.
 */
public final class PersistRequestCallableSql extends PersistRequest {

  private final SpiCallableSql callableSql;
  private int rowCount;
  private String bindLog;
  private CallableStatement cstmt;
  private BindParams bindParam;

  /**
   * Create.
   */
  public PersistRequestCallableSql(SpiEbeanServer server, CallableSql cs, SpiTransaction t, PersistExecute persistExecute) {
    super(server, t, persistExecute, cs.getLabel());
    this.type = PersistRequest.Type.CALLABLESQL;
    this.callableSql = (SpiCallableSql) cs;
  }

  @Override
  public void profile(long offset, int flushCount) {
    profileBase(EVT_CALLABLESQL, offset, "", flushCount);
  }

  @Override
  public int executeOrQueue() {
    return executeStatement();
  }

  @Override
  public int executeNow() {
    return persistExecute.executeSqlCallable(this);
  }

  /**
   * Return the CallableSql.
   */
  public SpiCallableSql callableSql() {
    return callableSql;
  }

  /**
   * The the log of bind values.
   */
  public void setBindLog(String bindLog) {
    this.bindLog = bindLog;
  }

  /**
   * Note the rowCount of the execution.
   */
  @Override
  public void checkRowCount(int count) {
    this.rowCount = count;
  }

  /**
   * Only called for insert with generated keys.
   */
  @Override
  public void setGeneratedKey(Object idValue) {
  }

  /**
   * Perform post execute processing for the CallableSql.
   */
  @Override
  public void postExecute() {
    if (startNanos > 0) {
      persistExecute.collectSqlCall(label, startNanos);
    }
    if (transaction.isLogSummary()) {
      transaction.logSummary("CallableSql label[{0}] rows[{1}] bind[{2}]", callableSql.getLabel(), rowCount, bindLog);
    }

    // register table modifications with the transaction event
    TransactionEventTable tableEvents = callableSql.transactionEventTable();
    if (tableEvents != null && !tableEvents.isEmpty()) {
      transaction.event().add(tableEvents);
    } else {
      transaction.markNotQueryOnly();
    }
  }

  /**
   * These need to be set for use with Non-batch execution. Specifically to
   * read registered out parameters and potentially handle the
   * executeOverride() method.
   */
  public void setBound(BindParams bindParam, CallableStatement cstmt) {
    this.bindParam = bindParam;
    this.cstmt = cstmt;
  }

  /**
   * Execute the statement in normal non batch mode.
   */
  public int executeUpdate() throws SQLException {
    // check to see if the execution has been overridden
    // only works in non-batch mode
    if (callableSql.executeOverride(cstmt)) {
      return -1;
      // // been overridden so just return the rowCount
      // rowCount = callableSql.getRowCount();
      // return rowCount;
    }
    rowCount = cstmt.executeUpdate();
    // only read in non-batch mode
    readOutParams();
    return rowCount;
  }

  private void readOutParams() throws SQLException {
    List<Param> list = bindParam.positionedParameters();
    int pos = 0;
    for (Param param : list) {
      pos++;
      if (param.isOutParam()) {
        Object outValue = cstmt.getObject(pos);
        param.setOutValue(outValue);
      }
    }
  }

  @Override
  public Object identifier() {
    return bindLog;
  }
}
