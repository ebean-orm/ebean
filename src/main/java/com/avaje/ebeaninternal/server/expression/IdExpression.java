package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class IdExpression implements SpiExpression {

  private static final long serialVersionUID = -3065936341718489842L;

  private final Object value;

  IdExpression(Object value) {
    this.value = value;
  }

  /**
   * Always returns false.
   */
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  public void addBindValues(SpiExpressionRequest request) {

    // 'flatten' EmbeddedId and multiple Id cases
    // into an array of the underlying scalar field values
    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    Object[] bindIdValues = r.getBeanDescriptor().getBindIdValues(value);
    for (int i = 0; i < bindIdValues.length; i++) {
      request.addBindValue(bindIdValues[i]);
    }
  }

  public void addSql(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    String idSql = r.getBeanDescriptor().getIdBinderIdSql();

    request.append(idSql).append(" ");
  }

  /**
   * No properties so this is just a unique static number.
   */
  public int queryAutoFetchHash() {
    // this number is unique for a given bean type
    // which is all that is required
    return IdExpression.class.getName().hashCode();
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    return queryAutoFetchHash();
  }

  public int queryBindHash() {
    return value.hashCode();
  }

}
