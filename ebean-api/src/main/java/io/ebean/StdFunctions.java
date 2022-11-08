package io.ebean;

import io.ebean.Query.Property;

import java.time.temporal.Temporal;

public final class StdFunctions {

  public static Property<Number> count(Property<?> property) {
    return Property.of("count(" + property + ")");
  }

  public static Property<Number> sum(Property<Number> property) {
    return Property.of("sum(" + property + ")");
  }

  public static <BT> Property<BT> avg(Property<BT> property) {
    return Property.of("avg(" + property + ")");
  }

  public static <BT> Property<BT> max(Property<BT> property) {
    return Property.of("max(" + property + ")");
  }

  public static <BT> Property<BT> min(Property<BT> property) {
    return Property.of("min(" + property + ")");
  }

  public static <BT> Property<BT> coalesce(Property<BT> property, Object value) {
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

  // -------------------------------------------------------------------------------------------- //
  // ---- Expressions --------------------------------------------------------------------------- //

  public static Expression eq(Property<Number> property, Number value) {
    return Expr.eq(property.toString(), value);
  }

  public static Expression eq(Property<String> property, String value) {
    return Expr.eq(property.toString(), value);
  }

  public static Expression eq(Property<Temporal> property, Temporal value) {
    return Expr.eq(property.toString(), value);
  }

  public static Expression eq(Property<Boolean> property, boolean value) {
    return Expr.eq(property.toString(), value);
  }

  public static Expression eq(Property<Object> property, Object value) {
    return Expr.eq(property.toString(), value);
  }

  //----

  public static Expression gt(Property<Number> property, Number value) {
    return Expr.gt(property.toString(), value);
  }

  public static Expression gt(Property<String> property, String value) {
    return Expr.gt(property.toString(), value);
  }

  public static Expression gt(Property<Temporal> property, Temporal value) {
    return Expr.gt(property.toString(), value);
  }

  public static Expression gt(Property<Object> property, Object value) {
    return Expr.gt(property.toString(), value);
  }

  //----

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
