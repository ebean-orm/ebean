package com.avaje.ebeaninternal.api;

import com.avaje.ebean.ExpressionFactory;

public interface SpiExpressionFactory extends ExpressionFactory {

  /**
   * Create another expression factory with a given sub path.
   */
  public ExpressionFactory createExpressionFactory();

}
