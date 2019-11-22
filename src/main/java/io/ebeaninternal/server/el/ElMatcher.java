package io.ebeaninternal.server.el;

import io.ebean.QueryDsl;
import io.ebeaninternal.api.filter.Expression3VL;
import io.ebeaninternal.api.filter.FilterContext;

/**
 * Interface for defining matches for filter expressions.
 */
public interface ElMatcher<T> {

  /**
   * Return true if the bean matches the expression (for current permutation only)
   */
  Expression3VL isMatch(T bean, FilterContext ctx);

  /**
   * Converts the matcher to a human readable string representation.
   */
  void toString(StringBuilder sb);

  <F extends QueryDsl<T, F>> void visitDsl(QueryDsl<T, F> target);
}
