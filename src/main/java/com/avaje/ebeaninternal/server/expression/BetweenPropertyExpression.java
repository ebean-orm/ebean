package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

/**
 * Between expression where a value is between two properties.
 */
class BetweenPropertyExpression implements SpiExpression {

  private static final long serialVersionUID = 2078918165221454910L;

  private static final String BETWEEN = " between ";

  private final String lowProperty;
  private final String highProperty;
  private final Object value;

  BetweenPropertyExpression(String lowProperty, String highProperty, Object value) {
    this.lowProperty = lowProperty;
    this.highProperty = highProperty;
    this.value = value;
  }

  protected String name(String propName) {
    return propName;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    ElPropertyDeploy elProp = desc.getElPropertyDeploy(name(lowProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }

    elProp = desc.getElPropertyDeploy(name(highProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }
  }

  public void addBindValues(SpiExpressionRequest request) {
    request.addBindValue(value);
  }

  public void addSql(SpiExpressionRequest request) {

    request.append(" ? ").append(BETWEEN).append(name(lowProperty)).append(" and ").append(name(highProperty));
  }

  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(BetweenPropertyExpression.class).add(lowProperty).add(highProperty);
    builder.bind(1);
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    queryAutoFetchHash(builder);
  }

  public int queryBindHash() {
    return value.hashCode();
  }
}
