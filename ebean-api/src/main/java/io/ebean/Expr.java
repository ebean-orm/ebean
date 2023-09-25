package io.ebean;

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
 * This provides a convenient way to create expressions for the default
 * database.
 * <p>
 * See also {@link DB#expressionFactory()}
 * </p>
 * <p>
 * Creates standard common expressions for using in a Query Where or Having
 * clause.
 * </p>
 *
 * @see Query#where()
 */
public final class Expr {

  private Expr() {
  }

  /**
   * Return the underlying expression factory.
   */
  public static ExpressionFactory factory() {
    return DB.expressionFactory();
  }

  /**
   * Equal To - property equal to the given value.
   */
  public static Expression eq(String propertyName, Object value) {
    return factory().eq(propertyName, value);
  }

  /**
   * Not Equal To - property not equal to the given value.
   */
  public static Expression ne(String propertyName, Object value) {
    return factory().ne(propertyName, value);
  }

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  public static Expression ieq(String propertyName, String value) {
    return factory().ieq(propertyName, value);
  }

  /**
   * In Range - {@code property >= value1 and property < value2}.
   * <p>
   * Unlike Between inRange is "half open" and usually more useful for use with dates or timestamps.
   * </p>
   */
  public static Expression inRange(String propertyName, Object value1, Object value2) {
    return factory().inRange(propertyName, value1, value2);
  }

  /**
   * Between - property between the two given values.
   */
  public static Expression between(String propertyName, Object value1, Object value2) {
    return factory().between(propertyName, value1, value2);
  }

  /**
   * Between - value between two given properties.
   */
  public static Expression between(String lowProperty, String highProperty, Object value) {
    return factory().betweenProperties(lowProperty, highProperty, value);
  }

  /**
   * Greater Than - property greater than the given value.
   */
  public static Expression gt(String propertyName, Object value) {
    return factory().gt(propertyName, value);
  }

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  public static Expression ge(String propertyName, Object value) {
    return factory().ge(propertyName, value);
  }

  /**
   * Less Than - property less than the given value.
   */
  public static Expression lt(String propertyName, Object value) {
    return factory().lt(propertyName, value);
  }

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  public static Expression le(String propertyName, Object value) {
    return factory().le(propertyName, value);
  }

  /**
   * Is Null - property is null.
   */
  public static Expression isNull(String propertyName) {
    return factory().isNull(propertyName);
  }

  /**
   * Is Not Null - property is not null.
   */
  public static Expression isNotNull(String propertyName) {
    return factory().isNotNull(propertyName);
  }

  /**
   * Case insensitive {@link #exampleLike(Object)}
   */
  public static ExampleExpression iexampleLike(Object example) {
    return factory().iexampleLike(example);
  }

  /**
   * Create the query by Example expression which is case sensitive and using
   * LikeType.RAW (you need to add you own wildcards % and _).
   */
  public static ExampleExpression exampleLike(Object example) {
    return factory().exampleLike(example);
  }

  /**
   * Create the query by Example expression specifying more options.
   */
  public static ExampleExpression exampleLike(Object example, boolean caseInsensitive, LikeType likeType) {
    return factory().exampleLike(example, caseInsensitive, likeType);
  }

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  public static Expression like(String propertyName, String value) {
    return factory().like(propertyName, value);
  }

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  public static Expression ilike(String propertyName, String value) {
    return factory().ilike(propertyName, value);
  }

  /**
   * Starts With - property like value%.
   */
  public static Expression startsWith(String propertyName, String value) {
    return factory().startsWith(propertyName, value);
  }

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  public static Expression istartsWith(String propertyName, String value) {
    return factory().istartsWith(propertyName, value);
  }

  /**
   * Ends With - property like %value.
   */
  public static Expression endsWith(String propertyName, String value) {
    return factory().endsWith(propertyName, value);
  }

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public static Expression iendsWith(String propertyName, String value) {
    return factory().iendsWith(propertyName, value);
  }

  /**
   * Contains - property like %value%.
   */
  public static Expression contains(String propertyName, String value) {
    return factory().contains(propertyName, value);
  }

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public static Expression icontains(String propertyName, String value) {
    return factory().icontains(propertyName, value);
  }

  /**
   * For collection properties that are empty (have not existing elements).
   */
  public static Expression isEmpty(String propertyName) {
    return factory().isEmpty(propertyName);
  }

  /**
   * For collection properties that are not empty (have existing elements).
   */
  public static Expression isNotEmpty(String propertyName) {
    return factory().isNotEmpty(propertyName);
  }

  /**
   * In - property has a value in the array of values.
   */
  public static Expression in(String propertyName, Object[] values) {
    return factory().in(propertyName, values);
  }

  /**
   * In - using a subQuery.
   */
  public static Expression in(String propertyName, Query<?> subQuery) {
    return factory().in(propertyName, subQuery);
  }

  /**
   * In - property has a value in the collection of values.
   */
  public static Expression in(String propertyName, Collection<?> values) {
    return factory().in(propertyName, values);
  }

  /**
   * In where null or empty values means that no predicate is added to the query.
   * <p>
   * That is, only add the IN predicate if the values are not null or empty.
   * <p>
   * Without this we typically need to code an <code>if</code> block to only add
   * the IN predicate if the collection is not empty like:
   * </p>
   *
   * <h3>Without inOrEmpty()</h3>
   * <pre>{@code
   *
   *   query.where() // add some predicates
   *     .eq("status", Status.NEW);
   *
   *   if (ids != null && !ids.isEmpty()) {
   *     query.where().in("customer.id", ids);
   *   }
   *
   *   query.findList();
   *
   * }</pre>
   *
   * <h3>Using inOrEmpty()</h3>
   * <pre>{@code
   *
   *   query.where()
   *     .eq("status", Status.NEW)
   *     .inOrEmpty("customer.id", ids)
   *     .findList();
   *
   * }</pre>
   */
  public static Expression inOrEmpty(String propertyName, Collection<?> values) {
    return factory().inOrEmpty(propertyName, values);
  }

  /**
   * Id Equal to - ID property is equal to the value.
   */
  public static Expression idEq(Object value) {
    return factory().idEq(value);
  }

  /**
   * All Equal - Map containing property names and their values.
   * <p>
   * Expression where all the property names in the map are equal to the
   * corresponding value.
   * </p>
   *
   * @param propertyMap a map keyed by property names.
   */
  public static Expression allEq(Map<String, Object> propertyMap) {
    return factory().allEq(propertyMap);
  }

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * </p>
   */
  public static Expression raw(String raw, Object value) {
    return factory().raw(raw, value);
  }

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * </p>
   */
  public static Expression raw(String raw, Object[] values) {
    return factory().raw(raw, values);
  }

  /**
   * Add raw expression with no parameters.
   */
  public static Expression raw(String raw) {
    return factory().raw(raw);
  }

  /**
   * And - join two expressions with a logical and.
   */
  public static Expression and(Expression expOne, Expression expTwo) {
    return factory().and(expOne, expTwo);
  }

  /**
   * Or - join two expressions with a logical or.
   */
  public static Expression or(Expression expOne, Expression expTwo) {
    return factory().or(expOne, expTwo);
  }

  /**
   * Negate the expression (prefix it with NOT).
   */
  public static Expression not(Expression exp) {
    return factory().not(exp);
  }

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public static <T> Junction<T> conjunction(Query<T> query) {
    return factory().conjunction(query);
  }

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public static <T> Junction<T> disjunction(Query<T> query) {
    return factory().disjunction(query);
  }
}
