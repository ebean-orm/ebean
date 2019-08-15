package io.ebeaninternal.server.grammer;

import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;

class EqlWhereAdapter<T> extends EqlWhereListener<T> {

  private final ExpressionList<T> where;
  private final ExpressionFactory expr;

  EqlWhereAdapter(ExpressionList<T> where, ExpressionFactory expr) {
    this.where = where;
    this.expr = expr;
  }

  @Override
  ExpressionList<T> peekExprList() {
    return where;
  }

  @Override
  ExpressionFactory expressionFactory() {
    return expr;
  }

  @Override
  Object namedParam(String substring) {
    return null;
  }
}
