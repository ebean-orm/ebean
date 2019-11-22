package io.ebean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
   * Execute the query returning a single result using the mapper.
   *
   * @param mapper Used to map each ResultSet row into the result object.
   */
  <T> T findOne(RowMapper<T> mapper);

  /**
   * Execute the query returning a list using the mapper.
   *
   * @param mapper Used to map each ResultSet row into the result object.
   */
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
  @Nonnull
  Optional<SqlRow> findOneOrEmpty();

  /**
   * Execute the query returning a single scalar attribute.
   * <pre>@{code
   *
   *   String sql = "select max(unit_price) from o_order_detail where order_qty > ?";
   *
   *   BigDecimal maxPrice = DB.sqlQuery(sql)
   *     .setParameter(1, 2)
   *     .findSingleAttribute(BigDecimal.class);
   *
   * }</pre>
   *
   * <p>
   * The attributeType can be any scalar type that Ebean supports (includes javax time types, Joda types etc).
   * </p>
   *
   * @param attributeType The type of the returned value
   */
  <T> T findSingleAttribute(Class<T> attributeType);

  /**
   * Execute the query returning a single BigDecimal value.
   * <p>
   * This is an alias for <code>findSingleAttribute(BigDecimal.class)</code>
   * </p>
   */
  BigDecimal findSingleDecimal();

  /**
   * Execute the query returning a single Long value.
   * <p>
   * This is an alias for <code>findSingleAttribute(Long.class)</code>
   * </p>
   */
  Long findSingleLong();

  /**
   * Execute the query returning a list of scalar attribute values.
   *
   * <pre>{@code
   *
   *   String sql =
   *   " select (unit_price * order_qty) " +
   *   " from o_order_detail " +
   *   " where unit_price > ? " +
   *   " order by (unit_price * order_qty) desc";
   *
   *   //
   *   List<BigDecimal> lineAmounts =
   *     DB.sqlQuery(sql)
   *       .setParameter(1, 3)
   *       .findSingleAttributeList(BigDecimal.class);
   *
   * }</pre>
   *
   * <p>
   * The attributeType can be any scalar type that Ebean supports (includes javax time types, Joda types etc).
   * </p>
   *
   * @param attributeType The type of the returned value
   */
  <T> List<T> findSingleAttributeList(Class<T> attributeType);

  /**
   * The same as bind for named parameters.
   */
  SqlQuery setParameter(String name, Object value);

  /**
   * Set one of more positioned parameters.
   * <p>
   * This is a convenient alternative to multiple calls setParameter().
   *
   * <pre>{@code
   *
   *   String sql = "select id, name from customer where name like ? and status = ?";
   *
   *   List<SqlRow> list =
   *     DB.sqlQuery(sql)
   *       .setParams("Rob", Status.NEW)
   *       .findList();
   *
   *
   *   // is the same as ...
   *
   *   List<SqlRow> list =
   *     DB.sqlQuery(sql)
   *       .setParameter(1, "Rob")
   *       .setParameter(2, "Status.NEW)
   *       .findList();
   *
   * }</pre>
   */
  SqlQuery setParams(Object... values);

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
