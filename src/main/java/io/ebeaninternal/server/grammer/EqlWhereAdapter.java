package io.ebeaninternal.server.grammer;

import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebeaninternal.server.util.ArrayStack;

class EqlWhereAdapter<T> extends EqlWhereListener<T> {

  private final ExpressionList<T> where;
  private final ExpressionFactory expr;
  private final Object[] params;

  private int paramIndex;

  EqlWhereAdapter(ExpressionList<T> where, ExpressionFactory expr, Object[] params) {
    this.where = where;
    this.expr = expr;
    this.params = params;
  }

  @Override
  ExpressionList<T> peekExprList() {
    if (whereStack == null) {
      whereStack = new ArrayStack<>();
      whereStack.push(where);
    }
    return whereStack.peek();
  }

  @Override
  ExpressionFactory expressionFactory() {
    return expr;
  }

  @Override
  Object positionParam(String paramPosition) {
    if ("?".equals(paramPosition)) {
      return params[paramIndex++];
    }
    final int pos = Integer.parseInt(paramPosition.substring(1));
    return params[pos -1];
  }

  @Override
  Object namedParam(String substring) {
    throw new RuntimeException("Not supported");
  }
}
