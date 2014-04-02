package com.avaje.ebeaninternal.server.expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.ExampleExpression;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Junction;
import com.avaje.ebean.LikeType;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiExpressionFactory;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Default Expression factory for creating standard expressions.
 */
public class DefaultExpressionFactory implements SpiExpressionFactory {

  private static final Object[] EMPTY_ARRAY = new Object[] {};


  public DefaultExpressionFactory() {
  }

  public ExpressionFactory createExpressionFactory(){
    return this;
  }

  public String getLang() {
    return "sql";
  }

  /**
   * Equal To - property equal to the given value.
   */
  public Expression eq(String propertyName, Object value) {
    if (value == null) {
      return isNull(propertyName);
    }
    return new SimpleExpression(propertyName, SimpleExpression.Op.EQ, value);
  }

  /**
   * Not Equal To - property not equal to the given value.
   */
  public Expression ne(String propertyName, Object value) {
    if (value == null) {
      return isNotNull(propertyName);
    }
    return new SimpleExpression(propertyName, SimpleExpression.Op.NOT_EQ, value);
  }

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  public Expression ieq(String propertyName, String value) {
    if (value == null) {
      return isNull(propertyName);
    }
    return new CaseInsensitiveEqualExpression(propertyName, value);
  }

  /**
   * Between - property between the two given values.
   */
  public Expression between(String propertyName, Object value1, Object value2) {

    return new BetweenExpression(propertyName, value1, value2);
  }

  /**
   * Between - value between two given properties.
   */
  public Expression betweenProperties(String lowProperty, String highProperty, Object value) {

    return new BetweenPropertyExpression(lowProperty, highProperty, value);
  }

  /**
   * Greater Than - property greater than the given value.
   */
  public Expression gt(String propertyName, Object value) {

    return new SimpleExpression(propertyName, SimpleExpression.Op.GT, value);
  }

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  public Expression ge(String propertyName, Object value) {

    return new SimpleExpression(propertyName, SimpleExpression.Op.GT_EQ, value);
  }

  /**
   * Less Than - property less than the given value.
   */
  public Expression lt(String propertyName, Object value) {

    return new SimpleExpression(propertyName, SimpleExpression.Op.LT, value);
  }

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  public Expression le(String propertyName, Object value) {

    return new SimpleExpression(propertyName, SimpleExpression.Op.LT_EQ, value);
  }

  /**
   * Is Null - property is null.
   */
  public Expression isNull(String propertyName) {

    return new NullExpression(propertyName, false);
  }

  /**
   * Is Not Null - property is not null.
   */
  public Expression isNotNull(String propertyName) {

    return new NullExpression(propertyName, true);
  }

  private EntityBean checkEntityBean(Object bean) {
    if (bean == null || (bean instanceof EntityBean == false)) {
      throw new IllegalStateException("Expecting an EntityBean");
    }
    return (EntityBean)bean;
  }
  
  /**
   * Case insensitive {@link #exampleLike(Object)}
   */
  public ExampleExpression iexampleLike(Object example) {
    return new DefaultExampleExpression(checkEntityBean(example), true, LikeType.RAW);
  }

  /**
   * Create the query by Example expression which is case sensitive and using
   * LikeType.RAW (you need to add you own wildcards % and _).
   */
  public ExampleExpression exampleLike(Object example) {
    return new DefaultExampleExpression(checkEntityBean(example), false, LikeType.RAW);
  }

  /**
   * Create the query by Example expression specifying more options.
   */
  public ExampleExpression exampleLike(Object example, boolean caseInsensitive, LikeType likeType) {
    return new DefaultExampleExpression(checkEntityBean(example), caseInsensitive, likeType);
  }

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  public Expression like(String propertyName, String value) {
    return new LikeExpression(propertyName, value, false, LikeType.RAW);
  }

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  public Expression ilike(String propertyName, String value) {
    return new LikeExpression(propertyName, value, true, LikeType.RAW);
  }

  /**
   * Starts With - property like value%.
   */
  public Expression startsWith(String propertyName, String value) {
    return new LikeExpression(propertyName, value, false, LikeType.STARTS_WITH);
  }

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  public Expression istartsWith(String propertyName, String value) {
    return new LikeExpression(propertyName, value, true, LikeType.STARTS_WITH);
  }

  /**
   * Ends With - property like %value.
   */
  public Expression endsWith(String propertyName, String value) {
    return new LikeExpression(propertyName, value, false, LikeType.ENDS_WITH);
  }

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public Expression iendsWith(String propertyName, String value) {
    return new LikeExpression(propertyName, value, true, LikeType.ENDS_WITH);
  }

  /**
   * Contains - property like %value%.
   */
  public Expression contains(String propertyName, String value) {
    return new LikeExpression(propertyName, value, false, LikeType.CONTAINS);
  }

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public Expression icontains(String propertyName, String value) {
    return new LikeExpression(propertyName, value, true, LikeType.CONTAINS);
  }

  /**
   * In - property has a value in the array of values.
   */
  public Expression in(String propertyName, Object[] values) {
    return new InExpression(propertyName, values);
  }

  /**
   * In - using a subQuery.
   */
  public Expression in(String propertyName, Query<?> subQuery) {
    return new InQueryExpression(propertyName, (SpiQuery<?>) subQuery);
  }

  /**
   * In - property has a value in the collection of values.
   */
  public Expression in(String propertyName, Collection<?> values) {
    return new InExpression(propertyName, values);
  }

  /**
   * Id Equal to - ID property is equal to the value.
   */
  public Expression idEq(Object value) {
    if (value == null) {
      throw new NullPointerException("The id value is null");
    }
    return new IdExpression(value);
  }

  /**
   * Id IN a list of id values.
   */
  public Expression idIn(List<?> idList) {
    return new IdInExpression(idList);
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
  public Expression allEq(Map<String, Object> propertyMap) {
    return new AllEqualsExpression(propertyMap);
  }

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * </p>
   */
  public Expression raw(String raw, Object value) {
    return new RawExpression(raw, new Object[] { value });
  }

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * </p>
   */
  public Expression raw(String raw, Object[] values) {
    return new RawExpression(raw, values);
  }

  /**
   * Add raw expression with no parameters.
   */
  public Expression raw(String raw) {
    return new RawExpression(raw, EMPTY_ARRAY);
  }

  /**
   * And - join two expressions with a logical and.
   */
  public Expression and(Expression expOne, Expression expTwo) {

    return new LogicExpression.And(expOne, expTwo);
  }

  /**
   * Or - join two expressions with a logical or.
   */
  public Expression or(Expression expOne, Expression expTwo) {

    return new LogicExpression.Or(expOne, expTwo);
  }

  /**
   * Negate the expression (prefix it with NOT).
   */
  public Expression not(Expression exp) {

    return new NotExpression(exp);
  }

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public <T> Junction<T> conjunction(Query<T> query) {

    return new JunctionExpression.Conjunction<T>(query, query.where());
  }

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public <T> Junction<T> disjunction(Query<T> query) {

    return new JunctionExpression.Disjunction<T>(query, query.where());
  }

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public <T> Junction<T> conjunction(Query<T> query, ExpressionList<T> parent) {

    return new JunctionExpression.Conjunction<T>(query, parent);
  }

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public <T> Junction<T> disjunction(Query<T> query, ExpressionList<T> parent) {

    return new JunctionExpression.Disjunction<T>(query, parent);
  }

}
