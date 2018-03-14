package io.ebean;

import io.ebean.search.Match;
import io.ebean.search.MultiMatch;
import io.ebean.search.TextCommonTerms;
import io.ebean.search.TextQueryString;
import io.ebean.search.TextSimple;

import java.util.Collection;
import java.util.Map;

/**
 * Expression factory for creating standard expressions.
 * <p>
 * Creates standard common expressions for using in a Query Where or Having
 * clause.
 * </p>
 * <p>
 * You will often not use this class directly but instead just add expressions
 * via the methods on ExpressionList such as
 * {@link ExpressionList#gt(String, Object)}.
 * </p>
 * <p>
 * The ExpressionList is returned from {@link Query#where()}.
 * </p>
 * <pre>{@code
 * // Example: fetch orders where status equals new or orderDate > lastWeek.
 *
 * Expression newOrLastWeek =
 *     Expr.or(Expr.eq("status", Order.Status.NEW),
 *             Expr.gt("orderDate", lastWeek));
 *
 * Query<Order> query = Ebean.createQuery(Order.class);
 * query.where().add(newOrLastWeek);
 * List<Order> list = query.findList();
 * ...
 * }</pre>
 *
 * @see Query#where()
 */
public interface ExpressionFactory {

  /**
   * Path exists - for the given path in a JSON document.
   */
  Expression jsonExists(String propertyName, String path);

  /**
   * Path does not exist - for the given path in a JSON document.
   */
  Expression jsonNotExists(String propertyName, String path);

  /**
   * Equal to - for the given path in a JSON document.
   */
  Expression jsonEqualTo(String propertyName, String path, Object val);

  /**
   * Not Equal to - for the given path in a JSON document.
   */
  Expression jsonNotEqualTo(String propertyName, String path, Object val);

  /**
   * Greater than - for the given path in a JSON document.
   */
  Expression jsonGreaterThan(String propertyName, String path, Object val);

  /**
   * Greater than or equal to - for the given path in a JSON document.
   */
  Expression jsonGreaterOrEqual(String propertyName, String path, Object val);

  /**
   * Less than - for the given path in a JSON document.
   */
  Expression jsonLessThan(String propertyName, String path, Object val);

  /**
   * Less than or equal to - for the given path in a JSON document.
   */
  Expression jsonLessOrEqualTo(String propertyName, String path, Object val);

  /**
   * Between - for the given path in a JSON document.
   */
  Expression jsonBetween(String propertyName, String path, Object lowerValue, Object upperValue);

  /**
   * Array contains all the given values.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  Expression arrayContains(String propertyName, Object... values);

  /**
   * Array does not contain the given values.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  Expression arrayNotContains(String propertyName, Object... values);

  /**
   * Array is empty - for the given array property.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  Expression arrayIsEmpty(String propertyName);

  /**
   * Array is not empty - for the given array property.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  Expression arrayIsNotEmpty(String propertyName);

  /**
   * Equal To - property equal to the given value.
   */
  Expression eq(String propertyName, Object value);

  /**
   * Not Equal To - property not equal to the given value.
   */
  Expression ne(String propertyName, Object value);

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  Expression ieq(String propertyName, String value);

  /**
   * Case Insensitive Equal To that allows for named parameter use.
   */
  Expression ieqObject(String propertyName, Object value);

  /**
   * Between - property between the two given values.
   */
  Expression between(String propertyName, Object value1, Object value2);

  /**
   * Between - value between two given properties.
   */
  Expression betweenProperties(String lowProperty, String highProperty, Object value);

  /**
   * Greater Than - property greater than the given value.
   */
  Expression gt(String propertyName, Object value);

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  Expression ge(String propertyName, Object value);

  /**
   * Less Than - property less than the given value.
   */
  Expression lt(String propertyName, Object value);

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  Expression le(String propertyName, Object value);

  /**
   * Is Null - property is null.
   */
  Expression isNull(String propertyName);

  /**
   * Is Not Null - property is not null.
   */
  Expression isNotNull(String propertyName);

  /**
   * Case insensitive {@link #exampleLike(Object)}
   */
  ExampleExpression iexampleLike(Object example);

  /**
   * Create the query by Example expression which is case sensitive and using
   * LikeType.RAW (you need to add you own wildcards % and _).
   */
  ExampleExpression exampleLike(Object example);

  /**
   * Create the query by Example expression specifying more options.
   */
  ExampleExpression exampleLike(Object example, boolean caseInsensitive, LikeType likeType);

  /**
   * Like with support for named parameters.
   */
  Expression like(String propertyName, Object value, boolean caseInsensitive, LikeType likeType);

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  Expression like(String propertyName, String value);

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  Expression ilike(String propertyName, String value);

  /**
   * Starts With - property like value%.
   */
  Expression startsWith(String propertyName, String value);

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  Expression istartsWith(String propertyName, String value);

  /**
   * Ends With - property like %value.
   */
  Expression endsWith(String propertyName, String value);

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  Expression iendsWith(String propertyName, String value);

  /**
   * Contains - property like %value%.
   */
  Expression contains(String propertyName, String value);

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  Expression icontains(String propertyName, String value);

  /**
   * In expression using pairs of value objects.
   */
  Expression inPairs(Pairs pairs);

  /**
   * In - property has a value in the array of values.
   */
  Expression in(String propertyName, Object[] values);

  /**
   * In - using a subQuery.
   */
  Expression in(String propertyName, Query<?> subQuery);

  /**
   * In - property has a value in the collection of values.
   */
  Expression in(String propertyName, Collection<?> values);

  /**
   * Not In - property has a value in the array of values.
   */
  Expression notIn(String propertyName, Object[] values);

  /**
   * Not In - property has a value in the collection of values.
   */
  Expression notIn(String propertyName, Collection<?> values);

  /**
   * Not In - using a subQuery.
   */
  Expression notIn(String propertyName, Query<?> subQuery);

  /**
   * Exists expression
   */
  Expression exists(Query<?> subQuery);

  /**
   * Not exists expression
   */
  Expression notExists(Query<?> subQuery);

  /**
   * Is empty expression for collection properties.
   */
  Expression isEmpty(String propertyName);

  /**
   * Is not empty expression for collection properties.
   */
  Expression isNotEmpty(String propertyName);

  /**
   * Id Equal to - ID property is equal to the value.
   */
  Expression idEq(Object value);

  /**
   * Id IN a list of Id values.
   */
  Expression idIn(Object... idValues);

  /**
   * Id IN a collection of Id values.
   */
  Expression idIn(Collection<?> idCollection);

  /**
   * All Equal - Map containing property names and their values.
   * <p>
   * Expression where all the property names in the map are equal to the
   * corresponding value.
   * </p>
   *
   * @param propertyMap a map keyed by property names.
   */
  Expression allEq(Map<String, Object> propertyMap);

  /**
   * Add expression for ANY of the given bit flags to be set.
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  Expression bitwiseAny(String propertyName, long flags);

  /**
   * Add expression for ALL of the given bit flags to be set.
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  Expression bitwiseAll(String propertyName, long flags);

  /**
   * Add bitwise AND expression of the given bit flags to compare with the match/mask.
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  Expression bitwiseAnd(String propertyName, long flags, long match);

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * </p>
   */
  Expression raw(String raw, Object value);

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * </p>
   */
  Expression raw(String raw, Object[] values);

  /**
   * Add raw expression with no parameters.
   */
  Expression raw(String raw);

  /**
   * Create a Text Match expression (currently doc store/Elastic only).
   */
  Expression textMatch(String propertyName, String search, Match options);

  /**
   * Create a Text Multi match expression (currently doc store/Elastic only).
   */
  Expression textMultiMatch(String query, MultiMatch options);

  /**
   * Create a text simple query expression (currently doc store/Elastic only).
   */
  Expression textSimple(String search, TextSimple options);

  /**
   * Create a text query string expression (currently doc store/Elastic only).
   */
  Expression textQueryString(String search, TextQueryString options);

  /**
   * Create a text common terms expression (currently doc store/Elastic only).
   */
  Expression textCommonTerms(String search, TextCommonTerms options);

  /**
   * And - join two expressions with a logical and.
   */
  Expression and(Expression expOne, Expression expTwo);

  /**
   * Or - join two expressions with a logical or.
   */
  Expression or(Expression expOne, Expression expTwo);

  /**
   * Negate the expression (prefix it with NOT).
   */
  Expression not(Expression exp);

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  <T> Junction<T> conjunction(Query<T> query);

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  <T> Junction<T> disjunction(Query<T> query);

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  <T> Junction<T> conjunction(Query<T> query, ExpressionList<T> parent);

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  <T> Junction<T> disjunction(Query<T> query, ExpressionList<T> parent);

  /**
   * Return a Text query junction for MUST, SHOULD or MUST NOT.
   * <p>
   * This is doc store Elastic only.
   * </p>
   */
  <T> Junction<T> junction(Junction.Type type, Query<T> query, ExpressionList<T> parent);

}
