package io.ebean.typequery;

import io.ebean.Expr;
import io.ebean.Expression;

public class StdExpressions {

  public static Expression gt(TQColumn property, Object value) {
    return Expr.gt(property.toString(), value);
  }

  public static Expression like(TQColumn property, String value) {
    return Expr.like(property.toString(), value);
  }

  public static Expression ilike(TQColumn property, String value) {
    return Expr.ilike(property.toString(), value);
  }

  public static Expression startsWith(TQColumn property, String value) {
    return Expr.startsWith(property.toString(), value);
  }

  public static Expression istartsWith(TQColumn property, String value) {
    return Expr.istartsWith(property.toString(), value);
  }

  public static Expression contains(TQColumn property, String value) {
    return Expr.contains(property.toString(), value);
  }

  public static Expression icontains(TQColumn property, String value) {
    return Expr.icontains(property.toString(), value);
  }
}
