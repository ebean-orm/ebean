package io.ebean;

import io.ebean.Query.Property;

import java.util.Collection;

@Deprecated(since = "experimental")
public final class StdOperators {

  // ---- Functions ---- //

  public static Property<Number> sum(Property<? extends Number> property) {
    return Property.of("sum(" + property + ")");
  }

  public static Property<Number> count(Property<?> property) {
    return Property.of("count(" + property + ")");
  }

  public static <T> Property<T> avg(Property<T> property) {
    return Property.of("avg(" + property + ")");
  }

  public static <T> Property<T> max(Property<T> property) {
    return Property.of("max(" + property + ")");
  }

  public static <T> Property<T> min(Property<T> property) {
    return Property.of("min(" + property + ")");
  }

  public static <T> Property<T> coalesce(Property<T> property, Object value) {
    return Property.of("coalesce(" + property.toString() + "," + sqlValue(value) + ")");
  }

  public static Property<String> lower(Property<String> property) {
    return Property.of("lower(" + property + ")");
  }

  public static Property<String> upper(Property<String> property) {
    return Property.of("upper(" + property + ")");
  }

  public static Property<String> concat(Property<?> property, Object... values) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("concat(").append(property.toString());
    for (Object value : values) {
      expression.append(",").append(sqlConcatString(value));
    }
    expression.append(")");
    return Property.of(expression.toString());
  }

  private static String sqlConcatString(Object value) {
    String asStr = String.valueOf(value);
    return (value instanceof Property || isSqlQuoted(asStr)) ? asStr : "'" + value + "'";
  }

  private static boolean isSqlQuoted(String asStr) {
    return asStr.length() > 0 && asStr.charAt(0) == '\'';
  }

  private static String sqlValue(Object value) {
    if (value instanceof Property || value instanceof Number) {
      return value.toString();
    } else {
      return "'" + value + "'";
    }
  }

  // ---- Operators ---- //

  public static <T> Expression eq(Property<T> property, T value) {
    return Expr.eq(property.toString(), value);
  }

  public static <T> Expression eq(Property<T> property, Query<?> subQuery) {
    return Expr.in(property.toString(), subQuery);
  }

  public static <T> Expression eqOrNull(Property<T> property, T value) {
    return Expr.factory().eqOrNull(property.toString(), value);
  }

  public static <T> Expression ne(Property<T> property, T value) {
    return Expr.ne(property.toString(), value);
  }

  public static <T> Expression ne(Property<T> property, Query<?> subQuery) {
    return Expr.ne(property.toString(), subQuery);
  }

  public static <T> Expression gt(Property<T> property, T value) {
    return Expr.gt(property.toString(), value);
  }

  public static <T> Expression gt(Property<T> property, Query<?> subQuery) {
    return Expr.gt(property.toString(), subQuery);
  }

  public static <T> Expression gtOrNull(Property<T> property, T value) {
    return Expr.factory().gtOrNull(property.toString(), value);
  }

  public static <T> Expression ge(Property<T> property, T value) {
    return Expr.ge(property.toString(), value);
  }

  public static <T> Expression ge(Property<T> property, Query<?> subQuery) {
    return Expr.ge(property.toString(), subQuery);
  }

  public static <T> Expression geOrNull(Property<T> property, T value) {
    return Expr.factory().geOrNull(property.toString(), value);
  }

  public static <T> Expression lt(Property<T> property, T value) {
    return Expr.lt(property.toString(), value);
  }

  public static <T> Expression lt(Property<T> property, Query<?> subQuery) {
    return Expr.lt(property.toString(), subQuery);
  }

  public static <T> Expression ltOrNull(Property<T> property, T value) {
    return Expr.factory().ltOrNull(property.toString(), value);
  }

  public static <T> Expression le(Property<T> property, T value) {
    return Expr.le(property.toString(), value);
  }

  public static <T> Expression le(Property<T> property, Query<?> subQuery) {
    return Expr.le(property.toString(), subQuery);
  }

  public static <T> Expression leOrNull(Property<T> property, T value) {
    return Expr.factory().leOrNull(property.toString(), value);
  }

  public static <T> Expression inRange(Property<T> property, T lowValue, T highValue) {
    return Expr.factory().inRange(property.toString(), lowValue, highValue);
  }

  public static <T> Expression inRange(Property<T> lowProperty, Property<T> highProperty, T value) {
    return Expr.factory().inRangeWith(lowProperty.toString(), highProperty.toString(), value);
  }

  public static <T> Expression inRange(Property<T> lowProperty, Property<T> property, Property<T> highProperty) {
    return Expr.factory().inRangeWithProperties(lowProperty.toString(), property.toString(), highProperty.toString());
  }

  public static <T> Expression in(Property<T> property, Collection<T> value) {
    return Expr.in(property.toString(), value);
  }

  public static <T> Expression in(Property<T> property, Query<?> subQuery) {
    return Expr.in(property.toString(), subQuery);
  }

  public static <T> Expression inOrEmpty(Property<T> property, Collection<T> value) {
    return Expr.inOrEmpty(property.toString(), value);
  }

  public static <T> Expression notIn(Property<T> property, Collection<T> value) {
    return Expr.factory().notIn(property.toString(), value);
  }

  public static <T> Expression notIn(Property<T> property, Query<?> subQuery) {
    return Expr.factory().notIn(property.toString(), subQuery);
  }

  // ---- String operators ---- //

  public static Expression like(Property<String> property, String value) {
    return Expr.like(property.toString(), value);
  }

  public static Expression ilike(Property<String> property, String value) {
    return Expr.ilike(property.toString(), value);
  }

  public static Expression startsWith(Property<String> property, String value) {
    return Expr.startsWith(property.toString(), value);
  }

  public static Expression istartsWith(Property<String> property, String value) {
    return Expr.istartsWith(property.toString(), value);
  }

  public static Expression contains(Property<String> property, String value) {
    return Expr.contains(property.toString(), value);
  }

  public static Expression icontains(Property<String> property, String value) {
    return Expr.icontains(property.toString(), value);
  }
}
