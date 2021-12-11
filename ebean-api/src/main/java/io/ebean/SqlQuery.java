package io.ebean;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Query object for performing native SQL queries that return SqlRow or directly read
 * ResultSet using a RowMapper.
 * <p>
 * The returned SqlRow objects are similar to a LinkedHashMap with some type
 * conversion support added.
 * </p>
 * <p>
 * Refer to {@link DtoQuery} for native sql queries returning DTO beans.
 * </p>
 * <p>
 * Refer to {@link Database#findNative(Class, String)} for native sql queries returning entity beans.
 * </p>
 *
 * <pre>{@code
 *
 *   // example using named parameters
 *
 *   String sql = "select id, name from customer where name like :name and status_code = :status";
 *
 *   List<SqlRow> list =
 *     DB.sqlQuery(sql)
 *       .setParameter("name", "Acme%")
 *       .setParameter("status", "ACTIVE")
 *       .findList();
 *
 * }</pre>
 */
@NonNullApi
public interface SqlQuery extends Serializable, CancelableQuery {

  /**
   * Execute the query returning a list.
   */
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
   * Deprecated migrate to use {@link #mapTo(RowMapper)}
   */
  @Deprecated
  <T> T findOne(RowMapper<T> mapper);

  /**
   * Deprecated migrate to use {@link #mapTo(RowMapper)}
   */
  @Deprecated
  <T> List<T> findList(RowMapper<T> mapper);

  /**
   * Execute the query reading each row from ResultSet using the RowConsumer.
   * <p>
   * This provides a low level option that reads directly from the JDBC ResultSet
   * and is good for processing very large results where (unlike findList) we don't
   * hold all the results in memory but instead can process row by row.
   * </p>
   *
   * <pre>{@code
   *
   *  String sql = "select id, name, status from customer order by name desc";
   *
   *  DB.sqlQuery(sql)
   *    .findEachRow((resultSet, rowNum) -> {
   *
   *      // read directly from ResultSet
   *
   *      long id = resultSet.getLong(1);
   *      String name = resultSet.getString(2);
   *
   *      // do something interesting with the data
   *
   *    });
   *
   * }</pre>
   *
   * @param consumer Used to read and process each ResultSet row.
   */
  void findEachRow(RowConsumer consumer);

  /**
   * Execute the query returning an optional row.
   */
  Optional<SqlRow> findOneOrEmpty();

  /**
   * Deprecated - migrate to <code>.mapToScalar(attributeType).findOne()</code>.
   * <pre>{@code
   *
   *    .mapToScalar(BigDecimal.class)
   *    .findOne();
   * }
   */
  @Deprecated
  <T> T findSingleAttribute(Class<T> attributeType);

  /**
   * Deprecated - migrate to <code>.mapToScalar(BigDecimal.class).findOne()</code>.
   * <pre>{@code
   *
   *    .mapToScalar(BigDecimal.class)
   *    .findOne();
   * }
   */
  @Deprecated
  BigDecimal findSingleDecimal();

  /**
   * Deprecated - migrate to <code>.mapToScalar(Long.class).findOne()</code>.
   * <pre>{@code
   *
   *    .mapToScalar(Long.class)
   *    .findOne();
   * }
   */
  @Deprecated
  Long findSingleLong();

  /**
   * Deprecated - migrate to <code>.mapToScalar(Long.class).findList()</code>.
   * <pre>{@code
   *
   *    .mapToScalar(Long.class)
   *    .findList();
   * }
   */
  @Deprecated
  <T> List<T> findSingleAttributeList(Class<T> attributeType);

  /**
   * Set one of more positioned parameters.
   * <p>
   * This is a convenient alternative to multiple calls to {@link #setParameter(Object)}.
   *
   * <pre>{@code
   *
   *   String sql = "select id, name from customer where name like ? and status = ?";
   *
   *   List<SqlRow> list =
   *     DB.sqlQuery(sql)
   *       .setParameters("Rob", Status.NEW)
   *       .findList();
   *
   *
   *   // effectively the same as ...
   *
   *       .setParameter("Rob")
   *       .setParameter("Status.NEW)
   *
   *   // and ...
   *
   *       .setParameter(1, "Rob")
   *       .setParameter(2, "Status.NEW)
   *
   * }</pre>
   */
  SqlQuery setParameters(Object... values);

  /**
   * Deprecated migrate to setParameters(Object... values)
   */
  @Deprecated
  SqlQuery setParams(Object... values);

  /**
   * Set the next bind parameter by position.
   * <pre>{@code
   *
   *   String sql = "select id, name from customer where name like ? and status = ?";
   *
   *   List<SqlRow> list =
   *     DB.sqlQuery(sql)
   *       .setParameter("Rob")
   *       .setParameter("Status.NEW)
   *       .findList();
   *
   *   // the same as ...
   *
   *       .setParameters("Rob", Status.NEW)
   *
   *   // and ...
   *
   *       .setParameter(1, "Rob")
   *       .setParameter(2, "Status.NEW)
   *
   * }</pre>
   *
   * <p>
   * When binding a collection of values into a IN expression we should use
   * indexed parameters like ?1, ?2, ?3 etc rather than just ?.
   * </p>
   *
   * <pre>{@code
   *
   *   String sql = "select c.id, c.name from customer c where c.name in (?1)";
   *
   *   List<SqlRow> rows = DB.sqlQuery(sql)
   *       .setParameter(asList("Rob", "Fiona", "Jack"))
   *       .findList();
   *
   *
   *   List<SqlRow> rows = DB.sqlQuery(sql)
   *       .setParameter(1, asList("Rob", "Fiona", "Jack"))
   *       .findList();
   * }</pre>
   *
   * @param value The value to bind
   */
  SqlQuery setParameter(Object value);

  /**
   * Bind the parameter by its index position (1 based like JDBC).
   * <p>
   * When binding a collection of values into a IN expression we should use
   * indexed parameters like ?1, ?2, ?3 etc rather than just ?.
   * </p>
   *
   * <pre>{@code
   *
   *   String sql = "select c.id, c.name from customer c where c.name in (?1)";
   *
   *   List<SqlRow> rows = DB.sqlQuery(sql)
   *       .setParameter(asList("Rob", "Fiona", "Jack"))
   *       .findList();
   *
   *
   *   List<SqlRow> rows = DB.sqlQuery(sql)
   *       .setParameter(1, asList("Rob", "Fiona", "Jack"))
   *       .findList();
   * }</pre>
   */
  SqlQuery setParameter(int position, Object value);

  /**
   * Bind the named parameter value.
   */
  SqlQuery setParameter(String name, Object value);

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

  /**
   * The query result maps to a single scalar value like Long, BigDecimal,
   * String, UUID, OffsetDateTime etc.
   * <p>
   * Any scalar type Ebean is aware of can be used including java time
   * types like Instant, LocalDate, OffsetDateTime, UUID, Inet, Cdir etc.
   *
   * <pre>{@code
   *
   *   String sql = " select min(updtime) from o_order_detail " +
   *                " where unit_price > ? and updtime is not null ";
   *
   *   OffsetDateTime minCreated = DB.sqlQuery(sql)
   *     .setParameter(42)
   *     .mapToScalar(OffsetDateTime.class)
   *     .findOne();
   *
   * }</pre>
   *
   * @param attributeType The type the result is returned as
   * @return The query to execute via findOne() findList() etc
   */
  <T> TypeQuery<T> mapToScalar(Class<T> attributeType);

  /**
   * Use a RowMapper to map the result to beans.
   *
   * @param mapper Maps rows to beans
   * @param <T>    The type of beans mapped to
   * @return The query to execute by findOne() findList() etc
   */
  <T> TypeQuery<T> mapTo(RowMapper<T> mapper);

  /**
   * Query mapping to single scalar values.
   *
   * @param <T> The type of the scalar values
   */
  interface TypeQuery<T> {

    /**
     * Return the single value.
     */
    @Nullable
    T findOne();

    /**
     * Return the single value that is optional.
     */
    Optional<T> findOneOrEmpty();

    /**
     * Return the list of values.
     */
    List<T> findList();

    /**
     * Find streaming the result effectively consuming a row at a time.
     */
    void findEach(Consumer<T> consumer);
  }
}
