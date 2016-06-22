package com.avaje.ebean;

/**
 * A SqlUpdate for executing insert update or delete statements.
 * <p>
 * Provides a simple way to execute raw SQL insert update or delete statements
 * without having to resort to JDBC.
 * </p>
 * <p>
 * Supports the use of positioned or named parameters and can automatically
 * notify Ebean of the table modified so that Ebean can maintain its cache.
 * </p>
 * <p>
 * Note that {@link #setAutoTableMod(boolean)} and
 * Ebean#externalModification(String, boolean, boolean, boolean)} can be to
 * notify Ebean of external changes and enable Ebean to maintain it's "L2"
 * server cache.
 * </p>
 * 
 * <pre class="code">
 * // example that uses 'named' parameters 
 * String s = &quot;UPDATE f_topic set post_count = :count where id = :id&quot;
 * SqlUpdate update = Ebean.createSqlUpdate(s);
 * update.setParameter(&quot;id&quot;, 1);
 * update.setParameter(&quot;count&quot;, 50);
 * 
 * int modifiedCount = Ebean.execute(update);
 * 
 * String msg = &quot;There where &quot; + modifiedCount + &quot;rows updated&quot;
 * </pre>
 * 
 * @see Update
 * @see SqlQuery
 * @see CallableSql
 */
public interface SqlUpdate {

  /**
   * Execute the update returning the number of rows modified.
   * <p>
   * After you have executed the SqlUpdate you can bind new variables using
   * {@link #setParameter(String, Object)} etc and then execute the SqlUpdate
   * again.
   * </p>
   * <p>
   * For JDBC batch processing refer to
   * {@link Transaction#setBatchMode(boolean)} and
   * {@link Transaction#setBatchSize(int)}.
   * </p>
   * 
   * @see Ebean#execute(SqlUpdate)
   */
  int execute();

  /**
   * Return true if eBean should automatically deduce the table modification
   * information and process it.
   * <p>
   * If this is true then cache invalidation and text index management are aware
   * of the modification.
   * </p>
   */
  boolean isAutoTableMod();

  /**
   * Set this to false if you don't want eBean to automatically deduce the table
   * modification information and process it.
   * <p>
   * Set this to false if you don't want any cache invalidation or text index
   * management to occur. You may do this when say you update only one column
   * and you know that it is not important for cached objects or text indexes.
   * </p>
   */
  SqlUpdate setAutoTableMod(boolean isAutoTableMod);

  /**
   * Return the label that can be seen in the transaction logs.
   */
  String getLabel();

  /**
   * Set a descriptive text that can be put into the transaction log.
   * <p>
   * Useful when identifying the statement in the transaction log.
   * </p>
   */
  SqlUpdate setLabel(String label);

  /**
   * Return the sql statement.
   */
  String getSql();

  /**
   * Return the generated sql that has named parameters converted to positioned parameters.
   */
  String getGeneratedSql();

  /**
   * Return the timeout used to execute this statement.
   */
  int getTimeout();

  /**
   * Set the timeout in seconds. Zero implies no limit.
   * <p>
   * This will set the query timeout on the underlying PreparedStatement. If the
   * timeout expires a SQLException will be throw and wrapped in a
   * PersistenceException.
   * </p>
   */
  SqlUpdate setTimeout(int secs);

  /**
   * Set a parameter via its index position.
   */
  SqlUpdate setParameter(int position, Object value);

  /**
   * Set a null parameter via its index position. Exactly the same as
   * {@link #setNull(int, int)}.
   */
  SqlUpdate setNull(int position, int jdbcType);

  /**
   * Set a null valued parameter using its index position.
   */
  SqlUpdate setNullParameter(int position, int jdbcType);

  /**
   * Set a named parameter value.
   */
  SqlUpdate setParameter(String name, Object param);

  /**
   * Set a named parameter that has a null value. Exactly the same as
   * {@link #setNullParameter(String, int)}.
   */
  SqlUpdate setNull(String name, int jdbcType);

  /**
   * Set a named parameter that has a null value.
   */
  SqlUpdate setNullParameter(String name, int jdbcType);

}