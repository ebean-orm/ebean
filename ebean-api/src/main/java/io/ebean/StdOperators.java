package io.ebean;

import io.ebean.Query.Property;

import java.util.Collection;

/**
 * Standard Operators for use with strongly typed query construction.
 * <p>
 * This is currently deemed to be experimental and subject to change.
 */
@Deprecated(since = "experimental")
public final class StdOperators {

  // ---- Functions ---- //

  /**
   * Sum of the given property.
   */
  public static Property<Number> sum(Property<? extends Number> property) {
    return Property.of("sum(" + property + ")");
  }

  /**
   * Count of the given property.
   */
  public static Property<Number> count(Property<?> property) {
    return Property.of("count(" + property + ")");
  }

  /**
   * Average of the given property.
   */
  public static <T> Property<T> avg(Property<T> property) {
    return Property.of("avg(" + property + ")");
  }

  /**
   * Max of the given property.
   */
  public static <T> Property<T> max(Property<T> property) {
    return Property.of("max(" + property + ")");
  }

  /**
   * Min of the given property.
   */
  public static <T> Property<T> min(Property<T> property) {
    return Property.of("min(" + property + ")");
  }

  /**
   * Coalesce of the property and value.
   */
  public static <T> Property<T> coalesce(Property<T> property, Object value) {
    return Property.of("coalesce(" + property.toString() + "," + sqlValue(value) + ")");
  }

  /**
   * Lower of the given property.
   */
  public static Property<String> lower(Property<String> property) {
    return Property.of("lower(" + property + ")");
  }

  /**
   * Upper of the given property.
   */
  public static Property<String> upper(Property<String> property) {
    return Property.of("upper(" + property + ")");
  }

  /**
   * Concat of the given property and values or other properties.
   */
  public static Property<String> concat(Property<?> property, Object... values) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("concat(").append(property.toString());
    for (Object value : values) {
      expression.append(',').append(sqlConcatString(value));
    }
    expression.append(')');
    return Property.of(expression.toString());
  }

  private static String sqlConcatString(Object value) {
    if (value instanceof Property) {
      return value.toString();
    } else {
      return sqlQuote(value);
    }
  }

  /**
   * Allows numbers to be unquoted.
   */
  private static String sqlValue(Object value) {
    if (value instanceof Property || value instanceof Number) {
      return value.toString();
    } else {
      return sqlQuote(value);
    }
  }

  /**
   * SQL quoted escaping single quotes.
   */
  private static String sqlQuote(Object value) {
    return "'" + String.valueOf(value).replace("'", "''") + "'";
  }

  // ---- Operators ---- //

  /**
   * Equal to - for a property and value.
   */
  public static <T> Expression eq(Property<T> property, T value) {
    return Expr.eq(property.toString(), value);
  }

  /**
   * Equal to - for a property and sub-query.
   */
  public static <T> Expression eq(Property<T> property, Query<?> subQuery) {
    return Expr.in(property.toString(), subQuery);
  }

  /**
   * Equal to or null - for a property and value.
   */
  public static <T> Expression eqOrNull(Property<T> property, T value) {
    return Expr.factory().eqOrNull(property.toString(), value);
  }

  /**
   * Not equal to - for a property and value.
   */
  public static <T> Expression ne(Property<T> property, T value) {
    return Expr.ne(property.toString(), value);
  }

  /**
   * Not equal to - for a property and sub-query.
   */
  public static <T> Expression ne(Property<T> property, Query<?> subQuery) {
    return Expr.ne(property.toString(), subQuery);
  }

  /**
   * Greater than - for a property and value.
   */
  public static <T> Expression gt(Property<T> property, T value) {
    return Expr.gt(property.toString(), value);
  }

  /**
   * Greater than - for a property and sub-query.
   */
  public static <T> Expression gt(Property<T> property, Query<?> subQuery) {
    return Expr.gt(property.toString(), subQuery);
  }

  /**
   * Greater than or null - for a property and value.
   */
  public static <T> Expression gtOrNull(Property<T> property, T value) {
    return Expr.factory().gtOrNull(property.toString(), value);
  }

  /**
   * Greater than or equal to - for a property and value.
   */
  public static <T> Expression ge(Property<T> property, T value) {
    return Expr.ge(property.toString(), value);
  }

  /**
   * Greater than or equal to - for a property and sub-query.
   */
  public static <T> Expression ge(Property<T> property, Query<?> subQuery) {
    return Expr.ge(property.toString(), subQuery);
  }

  /**
   * Greater than or null - for a property and value.
   */
  public static <T> Expression geOrNull(Property<T> property, T value) {
    return Expr.factory().geOrNull(property.toString(), value);
  }

  /**
   * Less than - for a property and value.
   */
  public static <T> Expression lt(Property<T> property, T value) {
    return Expr.lt(property.toString(), value);
  }

  /**
   * Less than - for a property and sub-query.
   */
  public static <T> Expression lt(Property<T> property, Query<?> subQuery) {
    return Expr.lt(property.toString(), subQuery);
  }

  /**
   * Less than or null - for a property and value.
   */
  public static <T> Expression ltOrNull(Property<T> property, T value) {
    return Expr.factory().ltOrNull(property.toString(), value);
  }

  /**
   * Greater than or equal to - for a property and value.
   */
  public static <T> Expression le(Property<T> property, T value) {
    return Expr.le(property.toString(), value);
  }

  /**
   * Greater than or equal to - for a property and sub-query.
   */
  public static <T> Expression le(Property<T> property, Query<?> subQuery) {
    return Expr.le(property.toString(), subQuery);
  }

  /**
   * Greater than or equal to or null - for a property and value.
   */
  public static <T> Expression leOrNull(Property<T> property, T value) {
    return Expr.factory().leOrNull(property.toString(), value);
  }

  /**
   * In range - for a property and values.
   */
  public static <T> Expression inRange(Property<T> property, T lowValue, T highValue) {
    return Expr.factory().inRange(property.toString(), lowValue, highValue);
  }

  /**
   * In range - for properties and a value.
   */
  public static <T> Expression inRange(Property<T> lowProperty, Property<T> highProperty, T value) {
    return Expr.factory().inRangeWith(lowProperty.toString(), highProperty.toString(), value);
  }

  /**
   * In range - for properties.
   */
  public static <T> Expression inRange(Property<T> lowProperty, Property<T> property, Property<T> highProperty) {
    return Expr.factory().inRangeWithProperties(lowProperty.toString(), property.toString(), highProperty.toString());
  }

  /**
   * In - for a given property and collection of values.
   */
  public static <T> Expression in(Property<T> property, Collection<T> value) {
    return Expr.in(property.toString(), value);
  }

  /**
   * In - for a given property and sub-query.
   */
  public static <T> Expression in(Property<T> property, Query<?> subQuery) {
    return Expr.in(property.toString(), subQuery);
  }

  /**
   * In or empty - for a given property and collection of values.
   */
  public static <T> Expression inOrEmpty(Property<T> property, Collection<T> value) {
    return Expr.inOrEmpty(property.toString(), value);
  }

  /**
   * Not In - for a given property and collection of values.
   */
  public static <T> Expression notIn(Property<T> property, Collection<T> value) {
    return Expr.factory().notIn(property.toString(), value);
  }

  /**
   * Not In - for a given property and sub-query.
   */
  public static <T> Expression notIn(Property<T> property, Query<?> subQuery) {
    return Expr.factory().notIn(property.toString(), subQuery);
  }

  // ---- String operators ---- //

  /**
   * Like - for a given property and value.
   */
  public static Expression like(Property<String> property, String value) {
    return Expr.like(property.toString(), value);
  }

  /**
   * Case-insensitive Like - for a given property and value.
   */
  public static Expression ilike(Property<String> property, String value) {
    return Expr.ilike(property.toString(), value);
  }

  /**
   * Starts with - for a given property and value.
   */
  public static Expression startsWith(Property<String> property, String value) {
    return Expr.startsWith(property.toString(), value);
  }

  /**
   * Case-insensitive starts with - for a given property and value.
   */
  public static Expression istartsWith(Property<String> property, String value) {
    return Expr.istartsWith(property.toString(), value);
  }

  /**
   * Contains - for a given property and value.
   */
  public static Expression contains(Property<String> property, String value) {
    return Expr.contains(property.toString(), value);
  }

  /**
   * Case-insensitive contains - for a given property and value.
   */
  public static Expression icontains(Property<String> property, String value) {
    return Expr.icontains(property.toString(), value);
  }
}
