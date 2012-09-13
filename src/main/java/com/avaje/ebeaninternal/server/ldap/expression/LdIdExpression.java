package com.avaje.ebeaninternal.server.ldap.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.util.DefaultExpressionRequest;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class LdIdExpression extends LdAbstractExpression implements SpiExpression {

  private static final long serialVersionUID = -3065936341718489843L;

  private final Object value;

  LdIdExpression(Object value) {
    super(null);
    this.value = value;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  public void addBindValues(SpiExpressionRequest request) {

    // 'flatten' EmbeddedId and multiple Id cases
    // into an array of the underlying scalar field values
    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanProperty[] propertiesId = r.getBeanDescriptor().propertiesId();
    if (propertiesId.length > 1) {
      throw new RuntimeException("Only single Id property is supported for LDAP");
    }
    request.addBindValue(value);
  }

  public void addSql(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    BeanProperty[] propertiesId = r.getBeanDescriptor().propertiesId();
    if (propertiesId.length > 1) {
      throw new RuntimeException("Only single Id property is supported for LDAP");
    }

    String ldapProp = propertiesId[0].getDbColumn();
    request.append(ldapProp).append("=").append(nextParam(request));
  }

  /**
   * No properties so this is just a unique static number.
   */
  public int queryAutoFetchHash() {
    // this number is unique for a given bean type
    // which is all that is required
    return LdIdExpression.class.getName().hashCode();
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    return queryAutoFetchHash();
  }

  public int queryBindHash() {
    return value.hashCode();
  }

}
