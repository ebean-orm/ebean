package io.ebean;

import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Query for performing native SQL queries that return DTO Bean's.
 * <p>
 * These beans are just normal classes. They must have public constructors
 * and setters.
 * <p>
 * Constructors with arguments are used if the number of constructor arguments
 * matches the number of columns in the resultSet.
 * </p>
 * <p>
 * If the number of columns in the resultSet is greater than the largest constructor
 * then the largest constructor is used for the first columns and remaining columns
 * are mapped by setter methods.
 * </p>
 *
 * <pre>{@code
 *
 *   // CustomerDto is just a 'bean like' class
 *   // with public constructor(s) and public setter methods
 *
 *   String sql = "select id, name from customer where name like :name and status_code = :status";
 *
 *   List<CustomerDto> beans =
 *     DB.findDto(CustomerDto.class, sql)
 *     .setParameter("name", "Acme%")
 *     .setParameter("status", "ACTIVE")
 *     .findList();
 *
 * }</pre>
 */
@NullMarked
public interface DtoQuery<T> extends StreamableQuery<DtoQuery<T>, T> {

  /**
   * Execute the query iterating a row at a time.
   * <p>
   * Note that the QueryIterator holds resources related to the underlying
   * resultSet and potentially connection and MUST be closed. We should use
   * QueryIterator in a <em>try with resource block</em>.
   */
  QueryIterator<T> findIterate();

  /**
   * Execute the query iterating a row at a time.
   * <p>
   * This streaming type query is useful for large query execution as only 1 row needs to be held in memory.
   * </p>
   */
  void findEach(Consumer<T> consumer);

  /**
   * Execute the query iterating the results and batching them for the consumer.
   * <p>
   * This runs like findEach streaming results from the database but just collects the results
   * into batches to pass to the consumer.
   *
   * @param batch    The number of dto beans to collect before given them to the consumer
   * @param consumer The consumer to process the batch of DTO beans
   */
  void findEach(int batch, Consumer<List<T>> consumer);

  /**
   * Execute the query iterating a row at a time with the ability to stop consuming part way through.
   * <p>
   * Returning false after processing a row stops the iteration through the query results.
   * </p>
   * <p>
   * This streaming type query is useful for large query execution as only 1 row needs to be held in memory.
   * </p>
   */
  void findEachWhile(Predicate<T> consumer);

  /**
   * Bind all the parameters using index positions.
   * <p>
   * Binds each parameter moving the index position each time.
   * <p>
   * A convenience for multiple calls to {@link #setParameter(Object)}
   */
  DtoQuery<T> setParameters(Object... value);

  /**
   * Bind the next parameter using index position.
   * <p>
   * Bind the parameter using index position starting at 1 and incrementing.
   * <p>
   */
  DtoQuery<T> setParameter(Object value);

  /**
   * Bind the named parameter.
   */
  DtoQuery<T> setParameter(String name, Object value);

  /**
   * Bind the named parameter to SQL NULL.
   */
  DtoQuery<T> setNullParameter(String name, int jdbcType);

  /**
   * Bind the named multi-value array parameter which we would use with Postgres ANY.
   * <p>
   * For Postgres this binds an ARRAY rather than expands into multiple bind values.
   * <pre>{@code
   *
   *   String sql = "select id, name from o_customer where id = any(:idList)";
   *
   *   var ids = List.of(1, 2, 3);
   *
   *   List<CustomerDto> list2 = DB.findDto(CustomerDto.class, sql)
   *       .setArrayParameter("idList", ids)
   *       .findList();
   *
   * }</pre>
   */
  DtoQuery<T> setArrayParameter(String name, Collection<?> values);

  /**
   * Bind the parameter by its index position (1 based like JDBC).
   */
  DtoQuery<T> setParameter(int position, Object value);

  /**
   * Set a positioned parameter to SQL NULL.
   */
  DtoQuery<T> setNullParameter(int position, int jdbcType);

  /**
   * Set the index of the first row of the results to return.
   */
  DtoQuery<T> setFirstRow(int firstRow);

  /**
   * Set the maximum number of query results to return.
   */
  DtoQuery<T> setMaxRows(int maxRows);

  /**
   * When resultSet columns are not able to be mapped to a bean property then instead of
   * throwing effectively skip reading that column.
   */
  DtoQuery<T> setRelaxedMode();

  /**
   * Set a label on the query to make it easier to identify queries related to query execution statistics.
   *
   * @param label A label that is unique to the DTO bean type.
   */
  DtoQuery<T> setLabel(String label);

  /**
   * Set the profile location of this query. This is used to relate query execution metrics
   * back to a location like a specific line of code.
   */
  DtoQuery<T> setProfileLocation(ProfileLocation profileLocation);

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
  DtoQuery<T> setTimeout(int secs);

  /**
   * A hint which for JDBC translates to the Statement.fetchSize().
   * <p>
   * Gives the JDBC driver a hint as to the number of rows that should be
   * fetched from the database when more rows are needed for ResultSet.
   * </p>
   */
  DtoQuery<T> setBufferFetchSizeHint(int bufferFetchSizeHint);

  /**
   * Return a PagedList for this query using firstRow and maxRows.
   * <p>
   * The benefit of using this over findList() is that it provides functionality to get the
   * total row count etc.
   * <p>
   * If maxRows is not set on the query prior to calling findPagedList() then a
   * PersistenceException is thrown.
   * <p>
   * This is only supported for a DtoQuery that is derived from an ORM query via
   * {@link Query#asDto(Class)} / {@link ExpressionList#asDto(Class)}. It is not supported
   * for a DtoQuery based on raw SQL (e.g. via {@link Database#findDto(Class, String)}) as
   * there is no query structure available from which to derive a matching row count query -
   * a PersistenceException is thrown in that case.
   * <pre>{@code
   *
   *  PagedList<OrderDto> pagedList =
   *    DB.find(Order.class)
   *       .where().eq("status", Order.Status.NEW)
   *       .orderBy().asc("id")
   *       .setFirstRow(50)
   *       .setMaxRows(20)
   *       .asDto(OrderDto.class)
   *       .findPagedList();
   *
   *       // fetch the total row count in the background
   *       pagedList.loadCount();
   *
   *       List<OrderDto> orders = pagedList.getList();
   *       int totalRowCount = pagedList.getTotalCount();
   *
   * }</pre>
   *
   * @return The PagedList
   */
  @Override
  PagedList<T> findPagedList();

}
