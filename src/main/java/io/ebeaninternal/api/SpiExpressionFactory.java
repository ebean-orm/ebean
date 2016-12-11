package io.ebeaninternal.api;

import io.ebean.ExpressionFactory;

public interface SpiExpressionFactory extends ExpressionFactory {

  /**
   * Create another expression factory with a given sub path.
   */
  ExpressionFactory createExpressionFactory();

}
