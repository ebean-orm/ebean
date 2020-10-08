package io.ebeaninternal.server.core;

import io.ebean.CallableSql;
import io.ebean.EbeanServer;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.BindParams.Param;
import io.ebeaninternal.api.SpiCallableSql;
import io.ebeaninternal.api.TransactionEventTable;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.SQLException;


public class DefaultCallableSql implements Serializable, SpiCallableSql {

  private static final long serialVersionUID = 8984272253185424701L;

  private transient final EbeanServer server;

  /**
   * The callable sql.
   */
  private String sql;

  /**
   * To display in the transaction log to help identify the procedure.
   */
  private String label;

  private int timeout;

  /**
   * Holds the table modification information. On commit this information is
   * used to manage the cache etc.
   */
  private final TransactionEventTable transactionEvent = new TransactionEventTable();

  private final BindParams bindParameters = new BindParams();

  /**
   * Create with callable sql.
   */
  public DefaultCallableSql(EbeanServer server, String sql) {
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
  public boolean executeOverride(CallableStatement cstmt) throws SQLException {
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
   * transaction after the transaction is commited.
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
