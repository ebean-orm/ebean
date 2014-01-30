package com.avaje.ebean;

import java.util.Collection;
import java.util.List;
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
 * 
 * <pre class="code">
 *  // Example: fetch orders where status equals new or orderDate > lastWeek.
 *  
 * Expression newOrLastWeek = 
 *   Expr.or(Expr.eq(&quot;status&quot;, Order.Status.NEW), 
 *           Expr.gt(&quot;orderDate&quot;, lastWeek));
 * 
 * Query&lt;Order&gt; query = Ebean.createQuery(Order.class);
 * query.where().add(newOrLastWeek);
 * List&lt;Order&gt; list = query.findList();
 * ...
 * </pre>
 * 
 * @see Query#where()
 */
public interface ExpressionFactory {

  /**
   * Equal To - property equal to the given value.
   */
  public Expression eq(String propertyName, Object value);

  /**
   * Not Equal To - property not equal to the given value.
   */
  public Expression ne(String propertyName, Object value);

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  public Expression ieq(String propertyName, String value);

  /**
   * Between - property between the two given values.
   */
  public Expression between(String propertyName, Object value1, Object value2);

  /**
   * Between - value between two given properties.
   */
  public Expression betweenProperties(String lowProperty, String highProperty, Object value);

  /**
   * Greater Than - property greater than the given value.
   */
  public Expression gt(String propertyName, Object value);

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  public Expression ge(String propertyName, Object value);

  /**
   * Less Than - property less than the given value.
   */
  public Expression lt(String propertyName, Object value);

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  public Expression le(String propertyName, Object value);

  /**
   * Is Null - property is null.
   */
  public Expression isNull(String propertyName);

  /**
   * Is Not Null - property is not null.
   */
  public Expression isNotNull(String propertyName);

  /**
   * Case insensitive {@link #exampleLike(Object)}
   */
  public ExampleExpression iexampleLike(Object example);

  /**
   * Create the query by Example expression which is case sensitive and using
   * LikeType.RAW (you need to add you own wildcards % and _).
   */
  public ExampleExpression exampleLike(Object example);

  /**
   * Create the query by Example expression specifying more options.
   */
  public ExampleExpression exampleLike(Object example, boolean caseInsensitive, LikeType likeType);

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  public Expression like(String propertyName, String value);

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  public Expression ilike(String propertyName, String value);

  /**
   * Starts With - property like value%.
   */
  public Expression startsWith(String propertyName, String value);

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  public Expression istartsWith(String propertyName, String value);

  /**
   * Ends With - property like %value.
   */
  public Expression endsWith(String propertyName, String value);

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public Expression iendsWith(String propertyName, String value);

  /**
   * Contains - property like %value%.
   */
  public Expression contains(String propertyName, String value);

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public Expression icontains(String propertyName, String value);

  /**
   * In - property has a value in the array of values.
   */
  public Expression in(String propertyName, Object[] values);

  /**
   * In - using a subQuery.
   */
  public Expression in(String propertyName, Query<?> subQuery);

  /**
   * In - property has a value in the collection of values.
   */
  public Expression in(String propertyName, Collection<?> values);

  /**
   * Id Equal to - ID property is equal to the value.
   */
  public Expression idEq(Object value);

  /**
   * Id IN a list of Id values.
   */
  public Expression idIn(List<?> idList);

  /**
   * All Equal - Map containing property names and their values.
   * <p>
   * Expression where all the property names in the map are equal to the
   * corresponding value.
   * </p>
   * 
   * @param propertyMap
   *          a map keyed by property names.
   */
  public Expression allEq(Map<String, Object> propertyMap);

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * </p>
   */
  public Expression raw(String raw, Object value);

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * </p>
   */
  public Expression raw(String raw, Object[] values);

  /**
   * Add raw expression with no parameters.
   */
  public Expression raw(String raw);

  /**
   * And - join two expressions with a logical and.
   */
  public Expression and(Expression expOne, Expression expTwo);

  /**
   * Or - join two expressions with a logical or.
   */
  public Expression or(Expression expOne, Expression expTwo);

  /**
   * Negate the expression (prefix it with NOT).
   */
  public Expression not(Expression exp);

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public <T> Junction<T> conjunction(Query<T> query);

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public <T> Junction<T> disjunction(Query<T> query);

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public <T> Junction<T> conjunction(Query<T> query, ExpressionList<T> parent);

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public <T> Junction<T> disjunction(Query<T> query, ExpressionList<T> parent);
}
