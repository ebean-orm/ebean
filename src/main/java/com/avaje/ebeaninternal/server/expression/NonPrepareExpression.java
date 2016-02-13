package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.SpiExpression;

/**
 * Base abstract expression that does nothing for prepareExpression().
 */
abstract class NonPrepareExpression implements SpiExpression {

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    // do nothing
  }
}
