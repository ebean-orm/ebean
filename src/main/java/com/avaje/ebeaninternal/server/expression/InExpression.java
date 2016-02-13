package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

import java.util.Collection;

class InExpression extends AbstractExpression {

  private static final long serialVersionUID = 3150665801693551260L;

  private final boolean not;

  private final Object[] values;

  InExpression(String propertyName, Collection<?> coll, boolean not) {
    super(propertyName);
    this.values = coll.toArray(new Object[coll.size()]);
    this.not = not;
  }

  InExpression(String propertyName, Object[] array, boolean not) {
    super(propertyName);
    this.values = array;
    this.not = not;
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null && !prop.isAssocId()) {
      prop = null;
    }

    for (int i = 0; i < values.length; i++) {
      if (prop == null) {
        request.addBindValue(values[i]);

      } else {
        // extract the id values from the bean
        Object[] ids = prop.getAssocOneIdValues((EntityBean) values[i]);
        if (ids != null) {
          for (int j = 0; j < ids.length; j++) {
            request.addBindValue(ids[j]);
          }
        }
      }
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    if (values.length == 0) {
      String expr = not ? "1=1" : "1=0";
      request.append(expr);
      return;
    }

    String propertyName = getPropertyName();

    ElPropertyValue prop = getElProp(request);
    if (prop != null && !prop.isAssocId()) {
      prop = null;
    }

    if (prop != null) {
      request.append(prop.getAssocIdInExpr(propertyName));
      String inClause = prop.getAssocIdInValueExpr(values.length);
      request.append(inClause);

    } else {
      request.append(propertyName);
      if (not) {
        request.append(" not");
      }
      request.append(" in (?");
      for (int i = 1; i < values.length; i++) {
        request.append(", ").append("?");
      }

      request.append(" ) ");
    }
  }

  /**
   * Based on the number of values in the in clause.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(InExpression.class).add(propName).add(values.length).add(not);
    builder.bind(values.length);
  }

  @Override
  public int queryBindHash() {
    int hc = 31;
    for (int i = 0; i < values.length; i++) {
      hc = 31 * hc + values[i].hashCode();
    }
    return hc;
  }

}
