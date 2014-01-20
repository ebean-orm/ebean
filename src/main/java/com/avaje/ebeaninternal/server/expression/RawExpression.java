package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

class RawExpression implements SpiExpression {

  private static final long serialVersionUID = 7973903141340334606L;

  private final String sql;

  private final Object[] values;

  RawExpression(String sql, Object[] values) {
    this.sql = sql;
    this.values = values;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  public void addBindValues(SpiExpressionRequest request) {
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        request.addBindValue(values[i]);
      }
    }
  }

  public void addSql(SpiExpressionRequest request) {
    request.append(sql);
  }

  /**
   * Based on the sql.
   */
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(RawExpression.class).add(sql);
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    queryAutoFetchHash(builder);
  }

  public int queryBindHash() {
    return sql.hashCode();
  }
}
