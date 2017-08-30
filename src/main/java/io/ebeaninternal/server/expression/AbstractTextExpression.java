package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

/**
 * Base class for TextExpressions that are only executable by doc store.
 * <p>
 * This means they can not be part of a SQL query nor do they use the built in query plan cache etc.
 * </p>
 */
public abstract class AbstractTextExpression extends AbstractExpression {

  AbstractTextExpression(String propName) {
    super(propName);
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always null for this expression
    return null;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    // do nothing, only execute against document store
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    // do nothing, only execute against document store
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    // do nothing, only execute against document store
  }

  @Override
  public int queryBindHash() {
    return 0;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    // do not compare by plan / bind values (this way)
    return false;
  }
}
