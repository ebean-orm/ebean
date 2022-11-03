package io.ebean.typequery;

import io.ebean.Query.Property;

public final class StdFunctions {

  public static Property max(Property property) {
    return new Standard("max(" + property + ")");
  }

  public static Property sum(Property property) {
    return new Standard("sum(" + property + ")");
  }

  public static Property concat(Property property, Object... values) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("concat(").append(property.toString());
    for (Object value : values) {
      expression.append(",").append(sqlStringExpression(value));
    }
    expression.append(")");
    return new Standard(expression.toString());
  }

  public static Property coalesce(Property property, Object value) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("coalesce(").append(property.toString()).append(",");
    expression.append(sqlStringExpression(value));
    expression.append(")");
    return new Standard(expression.toString());
  }

  private static String sqlStringExpression(Object value) {
    if (value instanceof Property || value instanceof Number) {
      return value.toString();
    } else {
      return "'" + value + "'";
    }
  }

  private static class Standard implements Property {

    private final String expression;

    private Standard(String expression) {
      this.expression = expression;
    }

    @Override
    public String toString() {
      return expression;
    }
  }
}
