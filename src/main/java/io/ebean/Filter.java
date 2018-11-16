package io.ebean;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Provides support for filtering and sorting lists of entities without going
 * back to the database.
 * <p>
 * That is, it uses local in-memory sorting and filtering of a list of entity
 * beans. It is not used in a Database query or invoke a Database query.
 * </p>
 * <p>
 * You can optionally specify a sortByClause and if so, the sort will always
 * execute prior to the filter expressions. You can specify any number of filter
 * expressions and they are effectively joined by logical "AND".
 * </p>
 * <p>
 * The result of the filter method will leave the original list unmodified and
 * return a new List instance.
 * </p>
 * <p>
 * <pre>{@code
 *
 * // get a list of entities (query execution statistics in this case)
 *
 * List<MetaQueryStatistic> list =
 *     Ebean.find(MetaQueryStatistic.class).findList();
 *
 * long nowMinus24Hrs = System.currentTimeMillis() - 24 * (1000 * 60 * 60);
 *
 * // sort and filter the list returning a filtered list...
 *
 * List<MetaQueryStatistic> filteredList =
 *     Ebean.filter(MetaQueryStatistic.class)
 *         .sort("avgTimeMicros desc")
 *         .gt("executionCount", 0)
 *         .gt("lastQueryTime", nowMinus24Hrs)
 *         .eq("autoTuned", true)
 *         .maxRows(10)
 *         .filter(list);
 *
 * }</pre>
 * <p>
 * The propertyNames can traverse the object graph (e.g. customer.name) by using
 * dot notation. If any point during the object graph traversal to get a
 * property value is null then null is returned.
 * </p>
 * <p>
 * <pre>{@code
 *
 * // examples of property names that
 * // ... will traverse the object graph
 * // ... where customer is a property of our bean
 *
 * customer.name
 * customer.shippingAddress.city
 *
 * }</pre>
 * <p>
 * <pre>{@code
 *
 * // get a list of entities (query execution statistics)
 *
 * List<Order> orders =
 *     Ebean.find(Order.class).findList();
 *
 * // Apply a filter...
 *
 * List<Order> filteredOrders =
 *     Ebean.filter(Order.class)
 *         .startsWith("customer.name", "Rob")
 *         .eq("customer.shippingAddress.city", "Auckland")
 *         .filter(orders);
 *
 * }</pre>
 *
 * @param <T> the entity bean type
 */
public interface Filter<T> extends QueryDsl<T,Filter<T>> {

  /**
   * Matcher to test if a bean will match. This holds a context internally, so that matching will happen efficient.
   *
   * @author Roland Praml, FOCONIS AG
   *
   * @param <T> the entity bean type.
   */
  interface Matcher<T> {
    boolean match(T bean);
  }

  /**
   * Specify a sortByClause.
   * <p>
   * The sort (if specified) will always execute first followed by the filter
   * expressions.
   * </p>
   * <p>
   * Refer to {@link Ebean#sort(List, String)} for more detail.
   * </p>
   */
  Filter<T> sort(String sortByClause);

  /**
   * Specify the first row to return.
   */
  Filter<T> firstRow(int firstRow);

  /**
   * Specify the maximum number of rows/elements to return.
   */
  Filter<T> maxRows(int maxRows);

  @Override
  Filter<T> eq(String prop, Object value);

  @Override
  Filter<T> ne(String propertyName, Object value);

  @Override
  Filter<T> ieq(String propertyName, String value);

  @Override
  Filter<T> between(String propertyName, Object value1, Object value2);

  @Override
  Filter<T> gt(String propertyName, Object value);

  @Override
  Filter<T> ge(String propertyName, Object value);

  @Override
  Filter<T> lt(String propertyName, Object value);

  @Override
  Filter<T> le(String propertyName, Object value);

  @Override
  Filter<T> isNull(String propertyName);

  @Override
  Filter<T> isNotNull(String propertyName);

  @Override
  Filter<T> startsWith(String propertyName, String value);

  @Override
  Filter<T> istartsWith(String propertyName, String value);

  @Override
  Filter<T> endsWith(String propertyName, String value);

  @Override
  Filter<T> iendsWith(String propertyName, String value);

  @Override
  Filter<T> contains(String propertyName, String value);

  @Override
  Filter<T> icontains(String propertyName, String value);

  /**
   * In - property has a value contained in the set of values.
   */
  default Filter<T> in(String propertyName, Set<?> values) {
    return in(propertyName, (Collection<?>)values);
  }

  /**
   * Apply the filter to the list returning a new list of the matching elements
   * in the sorted order.
   * <p>
   * The sourceList will remain unmodified.
   * </p>
   *
   * @return Returns a new list with the sorting and filters applied.
   */
  List<T> filter(List<T> sourceList);

  /**
   * returns the firstRow setting.
   */
  int getFirstRow();

  /**
   * retrns the maxRow setting.
   * @return
   */
  int getMaxRows();

  /**
   * returns the sortByClause.
   * @return
   */
  String getSort();

  /**
   * Returns a matcher.
   */
  Matcher<T> matcher();
}
