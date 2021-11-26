package io.ebean;

import java.util.Collection;

/**
 * A SqlUpdate for executing insert update or delete statements.
 * <p>
 * Provides a simple way to execute raw SQL insert update or delete statements
 * without having to resort to JDBC.
 * <p>
 * Supports the use of positioned or named parameters and can automatically
 * notify Ebean of the table modified so that Ebean can maintain its cache.
 * <p>
 * Note that {@link #setAutoTableMod(boolean)} and
 * Ebean#externalModification(String, boolean, boolean, boolean)} can be to
 * notify Ebean of external changes and enable Ebean to maintain it's "L2"
 * server cache.
 *
 * <h2>Positioned parameter example</h2>
 * <pre>{@code
 *
 *   // example using 'positioned' parameters
 *
 *   String sql = "insert into audit_log (group, title, description) values (?, ?, ?);
 *
 *   int rows =
 *     DB.sqlUpdate(sql)
 *       .setParams("login", "new user", "user rob was created")
 *       .executeNow();
 *
 * }</pre>
 *
 * <h2>Named parameter example</h2>
 * <pre>{@code
 *
 *   // example using 'named' parameters
 *
 *   String sql = "update topic set post_count = :count where id = :id";
 *
 *   int rows =
 *     DB.sqlUpdate(sql)
 *       .setParameter("id", 1)
 *       .setParameter("count", 50)
 *       .execute();
 *
 *   String msg = "There were " + rows + " rows updated";
 *
 * }</pre>
 *
 * <h2>Index parameter examples (e.g. ?1, ?2, ?3 ...)</h2>
 * <p>
 * We can use index parameters like ?1, ?2, ?3 etc when binding arrays/collections
 * of values into an IN expression.
 * </p>
 * <pre>{@code
 *
 *   // Binding a list of 3 values (9991, 9992, 9993) into an IN expression
 *
 *   DB.sqlUpdate("delete from o_customer where name = ? and id in (?2)")
 *     .setParameter(1, "Foo")
 *     .setParameter(2, asList(9991, 9992, 9993))
 *     .execute();
 *
 *   // note this effectively is the same as
 *
 *   DB.sqlUpdate("delete from o_customer where name = ? and id in (?2)")
 *     .setParameter("Foo")
 *     .setParameter(asList(9991, 9992, 9993))
 *     .execute();
 *
 * }</pre>
 *
 * <h3>Example: Using setParameter()</h3>
 * <pre>{@code
 *
 *  String sql = "insert into audit_log (id, description, modified_description) values (?,?,?)";
 *
 *  SqlUpdate insert = DB.sqlUpdate(sql);
 *
 *  try (Transaction txn = DB.beginTransaction()) {
 *    txn.setBatchMode(true);
 *
 *    insert.setParameter(10000);
 *    insert.setParameter("hello");
 *    insert.setParameter("rob");
 *    insert.execute();
 *
 *    insert.setParameter(10001);
 *    insert.setParameter("goodbye");
 *    insert.setParameter("rob");
 *    insert.execute();
 *
 *    insert.setParameter(10002);
 *    insert.setParameter("chow");
 *    insert.setParameter("bob");
 *    insert.execute();
 *
 *    txn.commit();
 *  }
 * }</pre>
 * <p>
 * An alternative to the batch mode on the transaction is to use addBatch() and executeBatch() like:
 * </p>
 * <pre>{@code
 *
 *   try (Transaction txn = DB.beginTransaction()) {
 *
 *     insert.setParameter(10000);
 *     insert.setParameter("hello");
 *     insert.setParameter("rob");
 *     insert.addBatch();
 *
 *     insert.setParameter(10001);
 *     insert.setParameter("goodbye");
 *     insert.setParameter("rob");
 *     insert.addBatch();
 *
 *     insert.setParameter(10002);
 *     insert.setParameter("chow");
 *     insert.setParameter("bob");
 *     insert.addBatch();
 *
 *     int[] rows = insert.executeBatch();
 *
 *     txn.commit();
 *   }
 *
 * }</pre>
 *
 * @see Update
 * @see SqlQuery
 * @see CallableSql
 */
public interface SqlUpdate {

  /**
   * Execute the update returning the number of rows modified.
   * <p>
   * Note that if the transaction has batch mode on then this update will use JDBC batch and may not execute until
   * later - at commit time or a transaction flush. In this case this method returns -1 indicating that the
   * update has been batched for later execution.
   * </p>
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
   */
  int execute();

  /**
   * Execute the statement now regardless of the JDBC batch mode of the transaction.
   */
  int executeNow();

  /**
   * Execute when addBatch() has been used to batch multiple bind executions.
   *
   * @return The row counts for each of the batched statements.
   */
  int[] executeBatch();

  /**
   * Add the statement to batch processing to then later execute via executeBatch().
   */
  void addBatch();

  /**
   * Return the generated key value.
   */
  Object getGeneratedKey();

  /**
   * Execute and return the generated key. This is effectively a short cut for:
   * <p>
   * <pre>{@code
   *
   *   sqlUpdate.execute();
   *   Object key = sqlUpdate.getGeneratedKey();
   *
   * }</pre>
   *
   * @return The generated key value
   */
  Object executeGetKey();

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
   * Set to true when we want to use getGeneratedKeys with this statement.
   */
  SqlUpdate setGetGeneratedKeys(boolean getGeneratedKeys);

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
   * Set one of more positioned parameters.
   * <p>
   * This is a convenient alternative to multiple setParameter() calls.
   *
   * <pre>{@code
   *
   *   String sql = "insert into audit_log (id, name, version) values (?,?,?)";
   *
   *   DB.sqlUpdate(sql)
   *       .setParameters(UUID.randomUUID(), "Hello", 1)
   *       .executeNow();
   *
   *
   *   // is the same as ...
   *
   *   DB.sqlUpdate(sql)
   *       .setParameter(UUID.randomUUID())
   *       .setParameter("Hello")
   *       .setParameter(1)
   *       .executeNow();
   *
   *   // which is the same as ...
   *
   *   DB.sqlUpdate(sql)
   *       .setParameter(1, UUID.randomUUID())
   *       .setParameter(2, "Hello")
   *       .setParameter(3, 1)
   *       .executeNow();
   *
   * }</pre>
   */
  SqlUpdate setParameters(Object... values);

  /**
   * Deprecated migrate to setParameters(Object... values).
   */
  @Deprecated
  SqlUpdate setParams(Object... values);

  /**
   * Set the next bind parameter by position.
   *
   * @param value The value to bind
   */
  SqlUpdate setParameter(Object value);

  /**
   * Deprecated migrate to setParameter(value).
   */
  @Deprecated
  SqlUpdate setNextParameter(Object value);

  /**
   * Set a parameter via its index position.
   */
  SqlUpdate setParameter(int position, Object value);

  /**
   * Set a null parameter via its index position.
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
   * Bind the named multi-value array parameter which we would use with Postgres ANY.
   * <p>
   * For Postgres this binds an ARRAY rather than expands into multiple bind values.
   */
  SqlUpdate setArrayParameter(String name, Collection<?> values);

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
