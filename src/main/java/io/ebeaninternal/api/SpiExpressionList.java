package io.ebeaninternal.api;

import io.ebean.ExpressionList;
import io.ebean.Junction;
import io.ebeaninternal.server.expression.DocQueryContext;

import java.io.IOException;
import java.util.List;

/**
 * Internal extension of ExpressionList.
 */
public interface SpiExpressionList<T> extends ExpressionList<T>, SpiExpression {

  /**
   * Return the expression list as a Junction (for ElasticSearch).
   */
  Junction<T> toJunction();

  /**
   * Return the underlying list of expressions.
   */
  List<SpiExpression> getUnderlyingList();

  /**
   * Return a copy of the ExpressionList with the path trimmed for filterMany() expressions.
   */
  SpiExpressionList<?> trimPath(int prefixTrim);

  /**
   * Return true if this list is empty.
   */
  boolean isEmpty();

  /**
   * Write the top level where expressions taking into account possible extra idEquals expression.
   */
  void writeDocQuery(DocQueryContext context, SpiExpression idEquals) throws IOException;
}
