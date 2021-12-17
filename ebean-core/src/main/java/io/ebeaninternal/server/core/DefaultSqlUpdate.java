package io.ebeaninternal.server.core;

import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.Update;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.api.SpiTransaction;

import java.io.Serializable;
import java.util.Collection;

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
   * The original sql update or delete statement.
   */
  private final String origSql;

  /**
   * The sql taking into account bind parameter expansion.
   */
  private String baseSql;

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

  private int bindExpansion;

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
    this.origSql = sql;
    this.baseSql = sql;
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
  public SpiSqlUpdate copy() {
    return new DefaultSqlUpdate(server, origSql, new BindParams());
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
      return DB.getDefault().execute(this);
    }
  }

  @Override
  public int executeNow() {
    if (server != null) {
      return server.executeNow(this);
    } else {
      throw new IllegalStateException("server is null?");
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
    this.baseSql = origSql;
    if (bindExpansion > 0) {
      bindParams.reset();
      bindExpansion = 0;
    }
  }

  @Override
  public String getSql() {
    return origSql;
  }

  @Override
  public String getBaseSql() {
    return baseSql;
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

  @Override
  public SqlUpdate setParams(Object... values) {
    return setParameters(values);
  }

  @Override
  public SqlUpdate setNextParameter(Object value) {
    return setParameter(value);
  }

  @Override
  public SqlUpdate setParameters(Object... values) {
    for (Object value : values) {
      setParameter(++addPos, value);
    }
    return this;
  }

  @Override
  public SqlUpdate setParameter(Object value) {
    setParameter(++addPos, value);
    return this;
  }

  private SqlUpdate setParamWithBindExpansion(int position, Collection<?> values, String bindLiteral) {
    StringBuilder sqlExpand = new StringBuilder(values.size() * 2);
    position = position + bindExpansion;
    int offset = 0;
    for (Object val : values) {
      if (offset > 0) {
        sqlExpand.append(",");
      }
      sqlExpand.append("?");
      bindParams.setParameter(position + offset++, val);
    }
    bindExpansion += (offset - 1);
    baseSql = baseSql.replace(bindLiteral, sqlExpand.toString());
    return this;
  }

  @Override
  public SqlUpdate setParameter(int position, Object value) {
    if (value instanceof Collection) {
      String bindLiteral = "?" + position;
      int pos = baseSql.indexOf(bindLiteral);
      if (pos > -1) {
        return setParamWithBindExpansion(position, (Collection<?>) value, bindLiteral);
      }
    }
    bindParams.setParameter(bindExpansion + position, value);
    return this;
  }

  @Override
  public SqlUpdate setNull(int position, int jdbcType) {
    bindParams.setNullParameter(bindExpansion + position, jdbcType);
    return this;
  }

  @Override
  public SqlUpdate setNullParameter(int position, int jdbcType) {
    return setNull(position, jdbcType);
  }

  @Override
  public SqlUpdate setParameter(String name, Object param) {
    bindParams.setParameter(name, param);
    return this;
  }

  @Override
  public SqlUpdate setArrayParameter(String name, Collection<?> values) {
    bindParams.setArrayParameter(name, values);
    return this;
  }

  @Override
  public SqlUpdate setNull(String name, int jdbcType) {
    bindParams.setNullParameter(name, jdbcType);
    return this;
  }

  @Override
  public SqlUpdate setNullParameter(String name, int jdbcType) {
    return setNull(name, jdbcType);
  }

  /**
   * Return the bind parameters.
   */
  @Override
  public BindParams getBindParams() {
    return bindParams;
  }

}
