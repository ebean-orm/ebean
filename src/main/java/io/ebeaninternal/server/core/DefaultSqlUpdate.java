package io.ebeaninternal.server.core;

import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Update;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;

import java.io.Serializable;

/**
 * A SQL Update Delete or Insert statement that can be executed. For the times
 * when you want to use Sql DML rather than a ORM bean approach. Refer to the
 * Ebean execute() method.
 * <p>
 * There is also {@link Update} which is similar except should use logical bean and
 * property names rather than physical table and column names.
 * </p>
 * <p>
 * SqlUpdate is designed for general DML sql and CallableSql is
 * designed for use with stored procedures.
 * </p>
 */
public final class DefaultSqlUpdate implements Serializable, SpiSqlUpdate {

  private static final long serialVersionUID = -6493829438421253102L;

  private transient final SpiEbeanServer server;

  /**
   * The parameters used to bind to the sql.
   */
  private final BindParams bindParams;

  /**
   * The sql update or delete statement.
   */
  private final String sql;

  /**
   * The actual sql with named parameters converted.
   */
  private String generatedSql;

  /**
   * Some descriptive text that can be put into the transaction log.
   */
  private String label = "";

  /**
   * The statement execution timeout.
   */
  private int timeout;

  /**
   * Automatically detect the table being modified by this sql. This will
   * register this information so that eBean invalidates cached objects if
   * required.
   */
  private boolean isAutoTableMod = true;

  /**
   * Helper to add positioned parameters in order.
   */
  private int addPos;

  private boolean getGeneratedKeys;

  private Object generatedKey;

  /**
   * Set when batching explicitly used.
   */
  private boolean batched;

  /**
   * Transaction used for addBatch() executeBatch() processing.
   */
  private transient SpiTransaction transaction;

  /**
   * Create with server sql and bindParams object.
   * <p>
   * Useful if you are building the sql and binding parameters at the
   * same time.
   * </p>
   */
  public DefaultSqlUpdate(SpiEbeanServer server, String sql, BindParams bindParams) {
    this.server = server;
    this.sql = sql;
    this.bindParams = bindParams;
  }

  /**
   * Create with a specific server. This means you can use the
   * SqlUpdate.execute() method.
   */
  public DefaultSqlUpdate(SpiEbeanServer server, String sql) {
    this(server, sql, new BindParams());
  }

  /**
   * Create with some sql.
   */
  public DefaultSqlUpdate(String sql) {
    this(null, sql, new BindParams());
  }

  @Override
  public void reset() {
    addPos = 0;
  }

  @Override
  public Object executeGetKey() {
    execute();
    return getGeneratedKey();
  }

  @Override
  public int execute() {
    if (server != null) {
      if (batched) {
        server.executeBatch(this, transaction);
        return -1;
      }
      return server.execute(this);
    } else {
      // Hopefully this doesn't catch anyone out...
      return Ebean.execute(this);
    }
  }

  @Override
  public int[] executeBatch() {
    if (server == null) {
      throw new IllegalStateException("No EbeanServer set?");
    }
    if (!batched) {
      throw new IllegalStateException("No prior addBatch() called?");
    }
    return server.executeBatch(this, transaction);
  }


  @Override
  public void addBatch() {
    if (server == null) {
      throw new IllegalStateException("No EbeanServer set?");
    }
    if (transaction == null) {
      transaction = server.currentServerTransaction();
      if (transaction == null) {
        throw new IllegalStateException("No current transaction? Must have a transaction to use addBatch()");
      }
    }

    batched = true;
    server.addBatch(this, transaction);
  }

  @Override
  public Object getGeneratedKey() {
    return generatedKey;
  }

  @Override
  public void setGeneratedKey(Object idValue) {
    this.generatedKey = idValue;
  }

  @Override
  public boolean isAutoTableMod() {
    return isAutoTableMod;
  }

  @Override
  public SqlUpdate setAutoTableMod(boolean isAutoTableMod) {
    this.isAutoTableMod = isAutoTableMod;
    return this;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public SqlUpdate setLabel(String label) {
    this.label = label;
    return this;
  }

  @Override
  public boolean isGetGeneratedKeys() {
    return getGeneratedKeys;
  }

  @Override
  public SqlUpdate setGetGeneratedKeys(boolean getGeneratedKeys) {
    this.getGeneratedKeys = getGeneratedKeys;
    return this;
  }

  @Override
  public String getGeneratedSql() {
    return generatedSql;
  }

  @Override
  public void setGeneratedSql(String generatedSql) {
    this.generatedSql = generatedSql;
  }

  @Override
  public String getSql() {
    return sql;
  }

  @Override
  public int getTimeout() {
    return timeout;
  }

  @Override
  public SqlUpdate setTimeout(int secs) {
    this.timeout = secs;
    return this;
  }

  public SqlUpdate setNextParameter(Object value) {
    setParameter(++addPos, value);
    return this;
  }

  @Override
  public SqlUpdate setParameter(int position, Object value) {
    bindParams.setParameter(position, value);
    return this;
  }

  @Override
  public SqlUpdate setNull(int position, int jdbcType) {
    bindParams.setNullParameter(position, jdbcType);
    return this;
  }

  @Override
  public SqlUpdate setNullParameter(int position, int jdbcType) {
    bindParams.setNullParameter(position, jdbcType);
    return this;
  }

  @Override
  public SqlUpdate setParameter(String name, Object param) {
    bindParams.setParameter(name, param);
    return this;
  }

  @Override
  public SqlUpdate setNull(String name, int jdbcType) {
    bindParams.setNullParameter(name, jdbcType);
    return this;
  }

  @Override
  public SqlUpdate setNullParameter(String name, int jdbcType) {
    bindParams.setNullParameter(name, jdbcType);
    return this;
  }

  /**
   * Return the bind parameters.
   */
  @Override
  public BindParams getBindParams() {
    return bindParams;
  }

}
