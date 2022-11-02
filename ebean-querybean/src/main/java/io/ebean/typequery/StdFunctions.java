package io.ebean.typequery;

public final class StdFunctions {

  public static TQColumn max(TQColumn property) {
    return new Standard("max(" + property + ")");
  }

  public static TQColumn sum(TQColumn property) {
    return new Standard("sum(" + property + ")");
  }

  public static TQColumn concat(TQColumn property, Object... values) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("concat(").append(property.toString());
    for (Object value : values) {
      expression.append(",").append(sqlStringExpression(value));
    }
    expression.append(")");
    return new Standard(expression.toString());
  }

  public static TQColumn coalesce(TQColumn property, Object value) {
    StringBuilder expression = new StringBuilder(50);
    expression.append("coalesce(").append(property.toString()).append(",");
    expression.append(sqlStringExpression(value));
    expression.append(")");
    return new Standard(expression.toString());
  }

  private static String sqlStringExpression(Object value) {
    if (value instanceof TQColumn || value instanceof Number) {
      return value.toString();
    } else {
      return "'" + value + "'";
    }
  }

  private static class Standard implements TQColumn {

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
