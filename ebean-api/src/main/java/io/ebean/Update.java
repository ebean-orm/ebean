package io.ebean;

/**
 * An Insert Update or Delete statement.
 * <p>
 * Generally a named update will be defined on the entity bean. This will take
 * the form of either an actual sql insert update delete statement or a similar
 * statement with bean name and property names in place of database table and
 * column names. The statement will likely include named parameters.
 * </p>
 * <p>
 * The following is an example of named updates on an entity bean.
 * </p>
 * <pre><code>
 *    ...
 *   &#64;NamedUpdates(value = {
 *     &#64;NamedUpdate(
 *       name = "setTitle",
 *       notifyCache = false,
 *       update = "update topic set title = :title, postCount = :count where id = :id"),
 *    &#64;NamedUpdate(
 *       name = "setPostCount",
 *       notifyCache = false,
 *       update = "update f_topic set post_count = :postCount where id = :id"),
 *    &#64;NamedUpdate(
 *       name = "incrementPostCount",
 *       notifyCache = false,
 *       update = "update Topic set postCount = postCount + 1 where id = :id")
 *       //update = "update f_topic set post_count = post_count + 1 where id = :id")
 *   })
 *   &#64;Entity
 *   &#64;Table(name = "f_topic")
 *   public class Topic {
 *     ...
 *   }
 * </code></pre>
 *
 * <p>
 * The following show code that would use a named update on the Topic entity
 * bean.
 * </p>
 * <p>
 * <pre>{@code
 *
 * Update<Topic> update = DB.createUpdate(Topic.class, "incrementPostCount");
 * update.setParameter("id", 1);
 * int rows = update.execute();
 *
 * }</pre>
 *
 * @param <T> the type of entity beans inserted updated or deleted
 */
public interface Update<T> {

  /**
   * Return the name if it is a named update.
   */
  String getName();

  /**
   * Set this to false if you do not want the cache to invalidate related
   * objects.
   * <p>
   * If you don't set this Ebean will automatically invalidate the appropriate
   * parts of the "L2" server cache.
   * </p>
   */
  Update<T> setNotifyCache(boolean notifyCache);

  /**
   * Set a timeout for statement execution.
   * <p>
   * This will typically result in a call to setQueryTimeout() on a
   * preparedStatement. If the timeout occurs an exception will be thrown - this
   * will be a SQLException wrapped up in a PersistenceException.
   * </p>
   *
   * @param secs the timeout in seconds. Zero implies unlimited.
   */
  Update<T> setTimeout(int secs);

  /**
   * Execute the statement returning the number of rows modified.
   */
  int execute();

  /**
   * Set an ordered bind parameter.
   * <p>
   * position starts at value 1 (not 0) to be consistent with PreparedStatement.
   * </p>
   * <p>
   * Set a value for each ? you have in the sql.
   * </p>
   *
   * @param position the index position of the parameter starting with 1.
   * @param value    the parameter value to bind.
   */
  Update<T> set(int position, Object value);

  /**
   * Set and ordered bind parameter (same as bind).
   *
   * @param position the index position of the parameter starting with 1.
   * @param value    the parameter value to bind.
   */
  Update<T> setParameter(int position, Object value);

  /**
   * Set an ordered parameter that is null. The JDBC type of the null must be
   * specified.
   * <p>
   * position starts at value 1 (not 0) to be consistent with PreparedStatement.
   * </p>
   */
  Update<T> setNull(int position, int jdbcType);

  /**
   * Set an ordered parameter that is null (same as bind).
   */
  Update<T> setNullParameter(int position, int jdbcType);

  /**
   * Set a named parameter. Named parameters have a colon to prefix the name.
   * <p>
   * A more succinct version of setParameter() to be consistent with Query.
   * </p>
   *
   * @param name  the parameter name.
   * @param value the parameter value.
   */
  Update<T> set(String name, Object value);

  /**
   * Bind a named parameter (same as bind).
   */
  Update<T> setParameter(String name, Object param);

  /**
   * Set a named parameter that is null. The JDBC type of the null must be
   * specified.
   * <p>
   * A more succinct version of setNullParameter().
   * </p>
   *
   * @param name     the parameter name.
   * @param jdbcType the type of the property being bound.
   */
  Update<T> setNull(String name, int jdbcType);

  /**
   * Bind a named parameter that is null (same as bind).
   */
  Update<T> setNullParameter(String name, int jdbcType);

  /**
   * Set a label meaning performance metrics will be collected for the execution of this update.
   */
  Update<T> setLabel(String label);

  /**
   * Return the sql that is actually executed.
   */
  String getGeneratedSql();

}
