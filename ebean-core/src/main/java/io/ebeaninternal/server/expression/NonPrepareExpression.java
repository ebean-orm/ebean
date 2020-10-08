package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.NaturalKeyQueryData;

/**
 * Base abstract expression that does nothing for prepareExpression().
 */
abstract class NonPrepareExpression implements SpiExpression {

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache
    return false;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    // do nothing
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always null in this expression
    return null;
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }
}
