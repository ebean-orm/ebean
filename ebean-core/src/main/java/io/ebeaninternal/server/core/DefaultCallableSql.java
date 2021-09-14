package io.ebeaninternal.server.core;

import io.ebean.CallableSql;
import io.ebean.Database;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.BindParams.Param;
import io.ebeaninternal.api.SpiCallableSql;
import io.ebeaninternal.api.TransactionEventTable;

import java.io.Serializable;
import java.sql.CallableStatement;

final class DefaultCallableSql implements Serializable, SpiCallableSql {

  private static final long serialVersionUID = 8984272253185424701L;

  /**
   * Holds the table modification information. On commit this information is
   * used to manage the cache etc.
   */
  private final TransactionEventTable transactionEvent = new TransactionEventTable();
  private final BindParams bindParameters = new BindParams();
  private transient final Database server;
  private String sql;
  private String label;
  private int timeout;

  DefaultCallableSql(Database server, String sql) {
    this.server = server;
    this.sql = sql;
  }

  public void execute() {
    server.execute(this, null);
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public CallableSql setLabel(String label) {
    this.label = label;
    return this;
  }

  @Override
  public int getTimeout() {
    return timeout;
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public CallableSql setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  @Override
  public CallableSql setSql(String sql) {
    this.sql = sql;
    return this;
  }

  @Override
  public CallableSql bind(int position, Object value) {
    bindParameters.setParameter(position, value);
    return this;
  }

  @Override
  public CallableSql setParameter(int position, Object value) {
    bindParameters.setParameter(position, value);
    return this;
  }

  @Override
  public CallableSql registerOut(int position, int type) {
    bindParameters.registerOut(position, type);
    return this;
  }

  @Override
  public Object getObject(int position) {
    Param p = bindParameters.getParameter(position);
    return p.getOutValue();
  }

  @Override
  public boolean executeOverride(CallableStatement statement) {
    return false;
  }

  @Override
  public CallableSql addModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    transactionEvent.add(tableName, inserts, updates, deletes);
    return this;
  }

  /**
   * Return the TransactionEvent which holds the table modification
   * information for this CallableSql. This information is merged into the
   * transaction after the transaction is committed.
   */
  @Override
  public TransactionEventTable getTransactionEventTable() {
    return transactionEvent;
  }

  @Override
  public BindParams getBindParams() {
    return bindParameters;
  }

}
