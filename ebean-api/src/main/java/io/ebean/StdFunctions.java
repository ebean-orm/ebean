package io.ebean;

import io.ebean.Query.Property;

public final class StdFunctions {

  public static Property avg(Property property) {
    return Property.of("avg(" + property + ")");
  }

  public static Property count(Property property) {
    return Property.of("count(" + property + ")");
  }

  public static Property max(Property property) {
    return Property.of("max(" + property + ")");
  }

  public static Property min(Property property) {
    return Property.of("min(" + property + ")");
  }

  public static Property sum(Property property) {
    return Property.of("sum(" + property + ")");
  }

  public static Property lower(Property property) {
    return Property.of("lower(" + property + ")");
  }

  public static Property upper(Property property) {
    return Property.of("upper(" + property + ")");
  }

  public static Property concat(Property property, Object... values) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("concat(").append(property.toString());
    for (Object value : values) {
      expression.append(",").append(sqlConcatString(value));
    }
    expression.append(")");
    return Property.of(expression.toString());
  }

  public static Property coalesce(Property property, Object value) {
    return Property.of("coalesce(" + property.toString() + "," + sqlValue(value) + ")");
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

  public static Expression eq(Property property, Object value) {
    return Expr.eq(property.toString(), value);
  }

  public static Expression gt(Property property, Object value) {
    return Expr.gt(property.toString(), value);
  }

  public static Expression like(Property property, String value) {
    return Expr.like(property.toString(), value);
  }

  public static Expression ilike(Property property, String value) {
    return Expr.ilike(property.toString(), value);
  }

  public static Expression startsWith(Property property, String value) {
    return Expr.startsWith(property.toString(), value);
  }

  public static Expression istartsWith(Property property, String value) {
    return Expr.istartsWith(property.toString(), value);
  }

  public static Expression contains(Property property, String value) {
    return Expr.contains(property.toString(), value);
  }

  public static Expression icontains(Property property, String value) {
    return Expr.icontains(property.toString(), value);
  }
}
