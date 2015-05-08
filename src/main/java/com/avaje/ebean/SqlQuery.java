package com.avaje.ebean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * 
 * <pre class="code">
 * // its typically a good idea to use a named query
 * // and put the sql in the orm.xml instead of in your code
 * 
 * String sql = &quot;select id, name from customer where name like :name and status_code = :status&quot;;
 * 
 * SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
 * sqlQuery.setParameter(&quot;name&quot;, &quot;Acme%&quot;);
 * sqlQuery.setParameter(&quot;status&quot;, &quot;ACTIVE&quot;);
 * 
 * // execute the query returning a List of MapBean objects
 * List&lt;SqlRow&gt; list = sqlQuery.findList();
 * </pre>
 * 
 */
public interface SqlQuery extends Serializable {

  /**
   * Cancel the query if support by the underlying database and driver.
   * <p>
   * This must be called from a different thread to the one executing the query.
   * </p>
   */
  void cancel();

  /**
   * Execute the query returning a list.
   */
  List<SqlRow> findList();

  /**
   * Execute the query returning a set.
   */
  Set<SqlRow> findSet();

  /**
   * Execute the query returning a map.
   */
  Map<?, SqlRow> findMap();

  /**
   * Execute the query returning a single row or null.
   * <p>
   * If this query finds 2 or more rows then it will throw a
   * PersistenceException.
   * </p>
   */
  SqlRow findUnique();

  /**
   * Execute find list SQL query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   * 
   * @return a Future object for the list result of the query
   * @deprecated
   */
  SqlFutureList findFutureList();

  /**
   * The same as bind for named parameters.
   */
  SqlQuery setParameter(String name, Object value);

  /**
   * The same as bind for positioned parameters.
   */
  SqlQuery setParameter(int position, Object value);

  /**
   * Set a listener to process the query on a row by row basis.
   * <p>
   * It this case the rows are not loaded into the persistence context and
   * instead can be processed by the query listener.
   * </p>
   * <p>
   * Use this when you want to process a large query and do not want to hold the
   * entire query result in memory.
   * </p>
   */
  SqlQuery setListener(SqlQueryListener queryListener);

  /**
   * Set the index of the first row of the results to return.
   */
  SqlQuery setFirstRow(int firstRow);

  /**
   * Set the maximum number of query results to return.
   */
  SqlQuery setMaxRows(int maxRows);

  /**
   * Set the index after which fetching continues in a background thread.
   */
  SqlQuery setBackgroundFetchAfter(int backgroundFetchAfter);

  /**
   * Set the column to use to determine the keys for a Map.
   */
  SqlQuery setMapKey(String mapKey);

  /**
   * Set a timeout on this query.
   * <p>
   * This will typically result in a call to setQueryTimeout() on a
   * preparedStatement. If the timeout occurs an exception will be thrown - this
   * will be a SQLException wrapped up in a PersistenceException.
   * </p>
   * 
   * @param secs
   *          the query timeout limit in seconds. Zero means there is no limit.
   */
  SqlQuery setTimeout(int secs);

  /**
   * A hint which for JDBC translates to the Statement.fetchSize().
   * <p>
   * Gives the JDBC driver a hint as to the number of rows that should be
   * fetched from the database when more rows are needed for ResultSet.
   * </p>
   */
  SqlQuery setBufferFetchSizeHint(int bufferFetchSizeHint);

}
