package com.avaje.ebean;

import java.util.Collection;
import java.util.Map;

/**
 * Expression factory for creating standard expressions for WHERE and HAVING
 * clauses.
 * <p>
 * Generally you will only need to use this object for creating OR, JUNCTION or
 * CONJUNCTION expressions. To create simple expressions you will most likely
 * just use the methods on the ExpressionList object that is returned via
 * {@link Query#where()}.
 * </p>
 * <p>
 * This provides a convenient way to create expressions for the 'Default'
 * server. It is actually a short cut for using the ExpressionFactory of the
 * 'default' EbeanServer.
 * <p>
 * See also {@link Ebean#getExpressionFactory()}
 * </p>
 * <p>
 * Creates standard common expressions for using in a Query Where or Having
 * clause.
 * </p>
 * 
 * <pre class="code">
 *  // Example: Using an Expr.or() method
 * Query&lt;Order&gt; query = Ebean.createQuery(Order.class);
 * query.where( 
 * 		Expr.or(Expr.eq(&quot;status&quot;, Order.NEW),
 *     		    Expr.gt(&quot;orderDate&quot;, lastWeek));
 *     
 * List&lt;Order&gt; list = query.findList();
 * ...
 * </pre>
 * 
 * @see Query#where()
 * @author Rob Bygrave
 */
public class Expr {

  private Expr() {
  }

  /**
   * Equal To - property equal to the given value.
   */
  public static Expression eq(String propertyName, Object value) {
    return Ebean.getExpressionFactory().eq(propertyName, value);
  }

  /**
   * Not Equal To - property not equal to the given value.
   */
  public static Expression ne(String propertyName, Object value) {
    return Ebean.getExpressionFactory().ne(propertyName, value);
  }

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  public static Expression ieq(String propertyName, String value) {
    return Ebean.getExpressionFactory().ieq(propertyName, value);
  }

  /**
   * Between - property between the two given values.
   */
  public static Expression between(String propertyName, Object value1, Object value2) {

    return Ebean.getExpressionFactory().between(propertyName, value1, value2);
  }

  /**
   * Between - value between two given properties.
   */
  public static Expression between(String lowProperty, String highProperty, Object value) {
    
    return Ebean.getExpressionFactory().betweenProperties(lowProperty, highProperty, value);
  }
  
  /**
   * Greater Than - property greater than the given value.
   */
  public static Expression gt(String propertyName, Object value) {
    return Ebean.getExpressionFactory().gt(propertyName, value);
  }

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  public static Expression ge(String propertyName, Object value) {
    return Ebean.getExpressionFactory().ge(propertyName, value);
  }

  /**
   * Less Than - property less than the given value.
   */
  public static Expression lt(String propertyName, Object value) {
    return Ebean.getExpressionFactory().lt(propertyName, value);
  }

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  public static Expression le(String propertyName, Object value) {
    return Ebean.getExpressionFactory().le(propertyName, value);
  }

  /**
   * Is Null - property is null.
   */
  public static Expression isNull(String propertyName) {
    return Ebean.getExpressionFactory().isNull(propertyName);
  }

  /**
   * Is Not Null - property is not null.
   */
  public static Expression isNotNull(String propertyName) {
    return Ebean.getExpressionFactory().isNotNull(propertyName);
  }

  /**
   * Case insensitive {@link #exampleLike(Object)}
   */
  public static ExampleExpression iexampleLike(Object example) {
    return Ebean.getExpressionFactory().iexampleLike(example);
  }

  /**
   * Create the query by Example expression which is case sensitive and using
   * LikeType.RAW (you need to add you own wildcards % and _).
   */
  public static ExampleExpression exampleLike(Object example) {
    return Ebean.getExpressionFactory().exampleLike(example);
  }

  /**
   * Create the query by Example expression specifying more options.
   */
  public static ExampleExpression exampleLike(Object example, boolean caseInsensitive,
      LikeType likeType) {
    return Ebean.getExpressionFactory().exampleLike(example, caseInsensitive, likeType);
  }

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  public static Expression like(String propertyName, String value) {
    return Ebean.getExpressionFactory().like(propertyName, value);
  }

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  public static Expression ilike(String propertyName, String value) {
    return Ebean.getExpressionFactory().ilike(propertyName, value);
  }

  /**
   * Starts With - property like value%.
   */
  public static Expression startsWith(String propertyName, String value) {
    return Ebean.getExpressionFactory().startsWith(propertyName, value);
  }

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  public static Expression istartsWith(String propertyName, String value) {
    return Ebean.getExpressionFactory().istartsWith(propertyName, value);
  }

  /**
   * Ends With - property like %value.
   */
  public static Expression endsWith(String propertyName, String value) {
    return Ebean.getExpressionFactory().endsWith(propertyName, value);
  }

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public static Expression iendsWith(String propertyName, String value) {
    return Ebean.getExpressionFactory().iendsWith(propertyName, value);
  }

  /**
   * Contains - property like %value%.
   */
  public static Expression contains(String propertyName, String value) {
    return Ebean.getExpressionFactory().contains(propertyName, value);
  }

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public static Expression icontains(String propertyName, String value) {
    return Ebean.getExpressionFactory().icontains(propertyName, value);
  }

  /**
   * In - property has a value in the array of values.
   */
  public static Expression in(String propertyName, Object[] values) {
    return Ebean.getExpressionFactory().in(propertyName, values);
  }

  /**
   * In - using a subQuery.
   */
  public static Expression in(String propertyName, Query<?> subQuery) {
    return Ebean.getExpressionFactory().in(propertyName, subQuery);
  }

  /**
   * In - property has a value in the collection of values.
   */
  public static Expression in(String propertyName, Collection<?> values) {
    return Ebean.getExpressionFactory().in(propertyName, values);
  }

  /**
   * Id Equal to - ID property is equal to the value.
   */
  public static Expression idEq(Object value) {
    return Ebean.getExpressionFactory().idEq(value);
  }

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
  public static Expression allEq(Map<String, Object> propertyMap) {
    return Ebean.getExpressionFactory().allEq(propertyMap);
  }

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * </p>
   */
  public static Expression raw(String raw, Object value) {
    return Ebean.getExpressionFactory().raw(raw, value);
  }

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * </p>
   */
  public static Expression raw(String raw, Object[] values) {
    return Ebean.getExpressionFactory().raw(raw, values);
  }

  /**
   * Add raw expression with no parameters.
   */
  public static Expression raw(String raw) {
    return Ebean.getExpressionFactory().raw(raw);
  }

  /**
   * And - join two expressions with a logical and.
   */
  public static Expression and(Expression expOne, Expression expTwo) {

    return Ebean.getExpressionFactory().and(expOne, expTwo);
  }

  /**
   * Or - join two expressions with a logical or.
   */
  public static Expression or(Expression expOne, Expression expTwo) {

    return Ebean.getExpressionFactory().or(expOne, expTwo);
  }

  /**
   * Negate the expression (prefix it with NOT).
   */
  public static Expression not(Expression exp) {

    return Ebean.getExpressionFactory().not(exp);
  }

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public static <T> Junction<T> conjunction(Query<T> query) {

    return Ebean.getExpressionFactory().conjunction(query);
  }

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public static <T> Junction<T> disjunction(Query<T> query) {

    return Ebean.getExpressionFactory().disjunction(query);
  }
}
