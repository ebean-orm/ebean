package io.ebean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Query object for performing native SQL queries that return SqlRow's.
 * <p>
 * Firstly note that you can use your own sql queries with <em>entity beans</em>
 * by using the SqlSelect annotation. This should be your first approach when
 * wanting to use your own SQL queries.
 * </p>
 * <p>
 * If ORM Mapping is too tight and constraining for your problem then SqlQuery
 * could be a good approach.
 * </p>
 * <p>
 * The returned SqlRow objects are similar to a LinkedHashMap with some type
 * conversion support added.
 * </p>
 * <p>
 * <pre>{@code
 *
 *   // its typically a good idea to use a named query
 *   // and put the sql in the orm.xml instead of in your code
 *
 *   String sql = "select id, name from customer where name like :name and status_code = :status";
 *
 *   SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
 *   sqlQuery.setParameter("name", "Acme%");
 *   sqlQuery.setParameter("status", "ACTIVE");
 *
 *   // execute the query returning a List of MapBean objects
 *   List<SqlRow> list = sqlQuery.findList();
 *
 * }</pre>
 */
public interface SqlQuery extends Serializable {

  /**
   * Execute the query returning a list.
   */
  @Nonnull
  List<SqlRow> findList();

  /**
   * Execute the SqlQuery iterating a row at a time.
   * <p>
   * This streaming type query is useful for large query execution as only 1 row needs to be held in memory.
   * </p>
   */
  void findEach(Consumer<SqlRow> consumer);

  /**
   * Execute the SqlQuery iterating a row at a time with the ability to stop consuming part way through.
   * <p>
   * Returning false after processing a row stops the iteration through the query results.
   * </p>
   * <p>
   * This streaming type query is useful for large query execution as only 1 row needs to be held in memory.
   * </p>
   */
  void findEachWhile(Predicate<SqlRow> consumer);

  /**
   * Execute the query returning a single row or null.
   * <p>
   * If this query finds 2 or more rows then it will throw a
   * PersistenceException.
   * </p>
   */
  @Nullable
  SqlRow findOne();

  /**
   * Execute the query returning an optional row.
   */
  @Nonnull
  Optional<SqlRow> findOneOrEmpty();

  /**
   * The same as bind for named parameters.
   */
  SqlQuery setParameter(String name, Object value);

  /**
   * The same as bind for positioned parameters.
   */
  SqlQuery setParameter(int position, Object value);

  /**
   * Set the index of the first row of the results to return.
   */
  SqlQuery setFirstRow(int firstRow);

  /**
   * Set the maximum number of query results to return.
   */
  SqlQuery setMaxRows(int maxRows);

  /**
   * Set a timeout on this query.
   * <p>
   * This will typically result in a call to setQueryTimeout() on a
   * preparedStatement. If the timeout occurs an exception will be thrown - this
   * will be a SQLException wrapped up in a PersistenceException.
   * </p>
   *
   * @param secs the query timeout limit in seconds. Zero means there is no limit.
   */
  SqlQuery setTimeout(int secs);

  /**
   * Set a label that can be put on performance metrics that are collected.
   */
  SqlQuery setLabel(String label);

  /**
   * A hint which for JDBC translates to the Statement.fetchSize().
   * <p>
   * Gives the JDBC driver a hint as to the number of rows that should be
   * fetched from the database when more rows are needed for ResultSet.
   * </p>
   */
  SqlQuery setBufferFetchSizeHint(int bufferFetchSizeHint);

}
