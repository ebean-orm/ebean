package io.ebean.typequery;

import io.ebean.Expr;
import io.ebean.Expression;
import io.ebean.Query;

public class StdExpressions {

  public static Expression gt(Query.Property property, Object value) {
    return Expr.gt(property.toString(), value);
  }

  public static Expression like(Query.Property property, String value) {
    return Expr.like(property.toString(), value);
  }

  public static Expression ilike(Query.Property property, String value) {
    return Expr.ilike(property.toString(), value);
  }

  public static Expression startsWith(Query.Property property, String value) {
    return Expr.startsWith(property.toString(), value);
  }

  public static Expression istartsWith(Query.Property property, String value) {
    return Expr.istartsWith(property.toString(), value);
  }

  public static Expression contains(Query.Property property, String value) {
    return Expr.contains(property.toString(), value);
  }

  public static Expression icontains(Query.Property property, String value) {
    return Expr.icontains(property.toString(), value);
  }
}
