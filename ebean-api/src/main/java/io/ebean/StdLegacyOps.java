package io.ebean;

import io.ebean.Query.Property;

import java.time.temporal.Temporal;

@Deprecated(since = "experimental")
public final class StdLegacyOps {

  public static Expression eq(Property<Temporal> property, java.util.Calendar value) {
    return Expr.eq(property.toString(), value);
  }

  public static Expression eqOrNull(Property<Temporal> property, java.util.Calendar value) {
    return Expr.factory().eqOrNull(property.toString(), value);
  }

  public static Expression ne(Property<Temporal> property, java.util.Calendar value) {
    return Expr.factory().ne(property.toString(), value);
  }

  public static Expression lt(Property<Temporal> property, java.util.Calendar value) {
    return Expr.factory().lt(property.toString(), value);
  }

  public static Expression ltOrNull(Property<Temporal> property, java.util.Calendar value) {
    return Expr.factory().ltOrNull(property.toString(), value);
  }

  public static Expression le(Property<Temporal> property, java.util.Calendar value) {
    return Expr.factory().le(property.toString(), value);
  }

  public static Expression leOrNull(Property<Temporal> property, java.util.Calendar value) {
    return Expr.factory().leOrNull(property.toString(), value);
  }
}
