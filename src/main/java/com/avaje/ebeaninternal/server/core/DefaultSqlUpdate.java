package com.avaje.ebeaninternal.server.core;

import java.io.Serializable;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Update;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiSqlUpdate;

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
 * 
 * <pre class="code">
 * // String sql = &quot;update f_topic set post_count = :count where id = :topicId&quot;;
 * 
 * SqlUpdate update = new SqlUpdate(sql);
 * update.setParameter(&quot;count&quot;, 1);
 * update.setParameter(&quot;topicId&quot;, 50);
 * 
 * int modifiedCount = Ebean.execute(update);
 * </pre>
 * 
 * <p>
 * Note that when the SqlUpdate is executed via Ebean.execute() the sql is
 * parsed to determine if it is an update, delete or insert. In addition the
 * table modified is deduced. If <em>isAutoTableMod()</em> is true, then this
 * is then added to the TransactionEvent and cache invalidation etc is
 * maintained. This means you don't need to use the Ebean.externalModification()
 * method as this has already been done.
 * </p>
 * <p>
 * You can sql.setAutoTableMod(false); to stop the automatic table modification
 * </p>
 * <p>
 * EXAMPLE: Using JDBC batching with SqlUpdate
 * </p>
 * <pre class="code">
 * 
 * String data = &quot;This is a simple test of the batch processing&quot;
 * 		+ &quot; mode and the transaction execute batch method&quot;;
 * 
 * String[] da = data.split(&quot; &quot;);
 * 
 * String sql = &quot;insert into junk (word) values (?)&quot;;
 * 
 * SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
 * 
 * Transaction t = Ebean.beginTransaction();
 * t.setBatchMode(true);
 * t.setBatchSize(3);
 * try {
 * 	for (int i = 0; i &lt; da.length; i++) {
 * 
 * 		sqlUpdate.setParameter(1, da[i]);
 * 		sqlUpdate.execute();
 * 	}
 * 
 * 	// NB: commit implicitly flushes the batch 
 * 	Ebean.commitTransaction();
 * 
 * } finally {
 * 	Ebean.endTransaction();
 * }
 * </pre> 
 * @see com.avaje.ebean.CallableSql
 * @see com.avaje.ebean.Ebean#execute(SqlUpdate)
 */
public final class DefaultSqlUpdate implements Serializable, SpiSqlUpdate {

	private static final long serialVersionUID = -6493829438421253102L;

	private transient final EbeanServer server;

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

	/**
	 * Create with server sql and bindParams object.
	 * <p>
	 * Useful if you are building the sql and binding parameters at the
	 * same time. 
	 * </p>
	 */
	public DefaultSqlUpdate(EbeanServer server, String sql, BindParams bindParams) {
		this.server = server;
		this.sql = sql;
		this.bindParams = bindParams;
	}
	
	/**
	 * Create with a specific server. This means you can use the
	 * SqlUpdate.execute() method.
	 */
	public DefaultSqlUpdate(EbeanServer server, String sql) {
		this(server, sql, new BindParams());
	}

	/**
	 * Create with some sql.
	 */
	public DefaultSqlUpdate(String sql) {
		this(null, sql, new BindParams());
	}

	public int execute() {
		if (server != null) {
			return server.execute(this);
		} else {
			// Hopefully this doesn't catch anyone out...
			return Ebean.execute(this);
		}
	}

	public boolean isAutoTableMod() {
		return isAutoTableMod;
	}

	public SqlUpdate setAutoTableMod(boolean isAutoTableMod) {
		this.isAutoTableMod = isAutoTableMod;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public SqlUpdate setLabel(String label) {
		this.label = label;
		return this;
	}

  public String getGeneratedSql() {
    return generatedSql;
  }

  @Override
  public void setGeneratedSql(String generatedSql) {
    this.generatedSql = generatedSql;
  }

  public String getSql() {
		return sql;
	}

	public int getTimeout() {
		return timeout;
	}

	public SqlUpdate setTimeout(int secs) {
		this.timeout = secs;
		return this;
	}
    
	public SqlUpdate addParameter(Object value) {
        return setParameter(++addPos, value);
    }
    
	public SqlUpdate setParameter(int position, Object value) {
		bindParams.setParameter(position, value);
		return this;
	}

	public SqlUpdate setNull(int position, int jdbcType) {
		bindParams.setNullParameter(position, jdbcType);
		return this;
	}

	public SqlUpdate setNullParameter(int position, int jdbcType) {
		bindParams.setNullParameter(position, jdbcType);
		return this;
	}

	public SqlUpdate setParameter(String name, Object param) {
		bindParams.setParameter(name, param);
		return this;
	}

	public SqlUpdate setNull(String name, int jdbcType) {
		bindParams.setNullParameter(name, jdbcType);
		return this;
	}

	public SqlUpdate setNullParameter(String name, int jdbcType) {
		bindParams.setNullParameter(name, jdbcType);
		return this;
	}

	/**
	 * Return the bind parameters.
	 */
	public BindParams getBindParams() {
		return bindParams;
	}

}
