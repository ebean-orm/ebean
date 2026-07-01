package io.ebean.typequery;


import org.jspecify.annotations.Nullable;
import io.ebean.Query;

/**
 * Base property for all comparable types.
 *
 * @param <R> the root query bean type
 * @param <T> the type of the scalar property
 */
public abstract class PBaseComparable<R, T> extends PBaseValueEqual<R, T> {

  /**
   * Construct with a property name and root instance.
   *
   * @param name property name
   * @param root the root query bean instance
   */
  public PBaseComparable(String name, R root) {
    super(name, root);
  }

  /**
   * Construct with additional path prefix.
   */
  public PBaseComparable(String name, R root, String prefix) {
    super(name, root, prefix);
  }

  // ---- range comparisons -------

  /**
   * Greater than.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R gt(T value) {
    expr().gt(_name, value);
    return _root;
  }

  /**
   * Greater than other property.
   * <p>
   * Note that the other property must have the same common "Base type" (String, Number, Temporal, Boolean or Object).
   *
   * @param other the other property to compare
   * @return the root query bean instance
   */
  public final R gt(Query.Property<T> other) {
    expr().raw(_name + " > " + other.toString());
    return _root;
  }

  /**
   * Greater than OR Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R gtOrNull(T value) {
    expr().gtOrNull(_name, value);
    return _root;
  }

  /**
   * Is greater than if value is non-null and otherwise no expression is added to the query.
   * <p>
   * That is, only add the GREATER THAN predicate if the value is not null.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>gtIfPresent()</code> rather than having a separate if block.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public final R gtIfPresent(@Nullable T value) {
    expr().gtIfPresent(_name, value);
    return _root;
  }


  /**
   * Greater than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R ge(T value) {
    expr().ge(_name, value);
    return _root;
  }

  /**
   * Greater than or Equal to other property.
   *
   * @param other the other property to compare
   * @return the root query bean instance
   */
  public final R ge(Query.Property<T> other) {
    expr().raw(_name + " >= " + other.toString());
    return _root;
  }

  /**
   * Greater than or Equal to OR Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R geOrNull(T value) {
    expr().geOrNull(_name, value);
    return _root;
  }

  /**
   * Is greater than or equal to if value is non-null and otherwise no expression is added to the query.
   * <p>
   * That is, only add the GREATER THAN OR EQUAL TO predicate if the value is not null.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>geIfPresent()</code> rather than having a separate if block.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public final R geIfPresent(@Nullable T value) {
    expr().geIfPresent(_name, value);
    return _root;
  }

  /**
   * Less than.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R lt(T value) {
    expr().lt(_name, value);
    return _root;
  }

  /**
   * Less than other property.
   *
   * @param other the other property to compare
   * @return the root query bean instance
   */
  public final R lt(Query.Property<T> other) {
    expr().raw(_name + " < " + other.toString());
    return _root;
  }

  /**
   * Less than OR Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R ltOrNull(T value) {
    expr().ltOrNull(_name, value);
    return _root;
  }

  /**
   * Is less than if value is non-null and otherwise no expression is added to the query.
   * <p>
   * That is, only add the LESS THAN predicate if the value is not null.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>ltIfPresent()</code> rather than having a separate if block.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public final R ltIfPresent(@Nullable T value) {
    expr().ltIfPresent(_name, value);
    return _root;
  }

  /**
   * Less than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R le(T value) {
    expr().le(_name, value);
    return _root;
  }

  /**
   * Less than or Equal to other property.
   *
   * @param other the other property to compare
   * @return the root query bean instance
   */
  public final R le(Query.Property<T> other) {
    expr().raw(_name + " <= " + other.toString());
    return _root;
  }

  /**
   * Less than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R leOrNull(T value) {
    expr().leOrNull(_name, value);
    return _root;
  }

  /**
   * Is less than or equal to if value is non-null and otherwise no expression is added to the query.
   * <p>
   * That is, only add the LESS THAN OR EQUAL TO predicate if the value is not null.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>leIfPresent()</code> rather than having a separate if block.
   *
   * @param value the value which can be null
   * @return the root query bean instance
   */
  public final R leIfPresent(@Nullable T value) {
    expr().leIfPresent(_name, value);
    return _root;
  }

  /**
   * Greater or equal to lower value and strictly less than upper value.
   * <p>
   * This is generally preferable over Between for date and datetime types
   * as SQL Between is inclusive on the upper bound ({@code <= }) and generally
   * we need the upper bound to be exclusive ({@code < }).
   * </p>
   *
   * @param lower the lower bind value ({@code >= })
   * @param upper the upper bind value ({@code < })
   * @return the root query bean instance
   */
  public final R inRange(T lower, T upper) {
    expr().inRange(_name, lower, upper);
    return _root;
  }

  /**
   * Value in Range between 2 properties.
   *
   * <pre>{@code
   *
   *    .startDate.inRangeWith(endDate, now)
   *
   *    // which equates to
   *    startDate <= now and (endDate > now or endDate is null)
   *
   * }</pre>
   *
   * <p>
   * This is a convenience expression combining a number of simple expressions.
   * The most common use of this could be called "effective dating" where 2 date or
   * timestamp columns represent the date range in which
   */
  public final R inRangeWith(Query.Property<T> highProperty, T value) {
    expr().inRangeWith(_name, highProperty.toString(), value);
    return _root;
  }

  /**
   * A Property is in Range between 2 other properties.
   *
   * <pre>{@code
   *  var o = QOrder.alias();
   *
   *  new QOrder()
   *    .orderDate.inRangeWith(o.product.startDate, o.product.endDate)
   *    .findList();
   *
   *    // which equates to
   *    product.startDate <= orderDate and (product.endDate > orderDate or product.endDate is null)
   *
   * }</pre>
   *
   * <p>
   * This is a convenience expression combining a number of simple expressions.
   */
  public final R inRangeWith(Query.Property<T> lowProperty, Query.Property<T> highProperty) {
    expr().inRangeWithProperties(_name, lowProperty.toString(), highProperty.toString());
    return _root;
  }

  /**
   * Between lower and upper values.
   *
   * @param lower the lower bind value
   * @param upper the upper bind value
   * @return the root query bean instance
   */
  public final R between(T lower, T upper) {
    expr().between(_name, lower, upper);
    return _root;
  }

  /**
   * Between - value between this property and another property
   */
  public final R betweenProperties(Query.Property<T> highProperty, T value) {
    expr().betweenProperties(_name, highProperty.toString(), value);
    return _root;
  }

  /**
   * Greater than.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R greaterThan(T value) {
    expr().gt(_name, value);
    return _root;
  }

  /**
   * Greater than or Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R greaterThanOrNull(T value) {
    expr().gtOrNull(_name, value);
    return _root;
  }

  /**
   * Greater than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R greaterOrEqualTo(T value) {
    expr().ge(_name, value);
    return _root;
  }

  /**
   * Less than.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R lessThan(T value) {
    expr().lt(_name, value);
    return _root;
  }

  /**
   * Less than or Null.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R lessThanOrNull(T value) {
    expr().ltOrNull(_name, value);
    return _root;
  }

  /**
   * Less than or Equal to.
   *
   * @param value the bind value
   * @return the root query bean instance
   */
  public final R lessOrEqualTo(T value) {
    expr().le(_name, value);
    return _root;
  }

  /**
   * Property is Less Than or Equal To the result of a sub-query.
   *
   * @param subQuery value provided by a subQuery
   * @return the root query bean instance
   */
  public final R le(Query<?> subQuery) {
    expr().le(_name, subQuery);
    return _root;
  }

  /**
   * Property is Less Than the result of a sub-query.
   *
   * @param subQuery value provided by a subQuery
   * @return the root query bean instance
   */
  public final R lt(Query<?> subQuery) {
    expr().lt(_name, subQuery);
    return _root;
  }

  /**
   * Property is Greater Than or Equal To the result of a sub-query.
   *
   * @param subQuery value provided by a subQuery
   * @return the root query bean instance
   */
  public final R ge(Query<?> subQuery) {
    expr().ge(_name, subQuery);
    return _root;
  }

  /**
   * Property is Greater Than the result of a sub-query.
   *
   * @param subQuery value provided by a subQuery
   * @return the root query bean instance
   */
  public final R gt(Query<?> subQuery) {
    expr().gt(_name, subQuery);
    return _root;
  }

  /**
   * Less Than or Equal To a raw SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  public final R leSubQuery(String sqlSubQuery, Object... bindValues) {
    expr().leSubQuery(_name, sqlSubQuery, bindValues);
    return _root;
  }

  /**
   * Less Than a raw SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  public final R ltSubQuery(String sqlSubQuery, Object... bindValues) {
    expr().ltSubQuery(_name, sqlSubQuery, bindValues);
    return _root;
  }

  /**
   * Greater Than or Equal To a raw SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  public final R geSubQuery(String sqlSubQuery, Object... bindValues) {
    expr().geSubQuery(_name, sqlSubQuery, bindValues);
    return _root;
  }

  /**
   * Greater Than a raw SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  public final R gtSubQuery(String sqlSubQuery, Object... bindValues) {
    expr().gtSubQuery(_name, sqlSubQuery, bindValues);
    return _root;
  }
}
