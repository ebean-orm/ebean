package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class InExpression extends AbstractExpression {

  private final boolean not;

  private final Collection<?> sourceValues;

  private Object[] bindValues;

  InExpression(String propertyName, Collection<?> sourceValues, boolean not) {
    super(propertyName);
    this.sourceValues = sourceValues;
    this.not = not;
  }

  InExpression(String propertyName, Object[] array, boolean not) {
    super(propertyName);
    this.sourceValues = Arrays.asList(array);
    this.not = not;
  }

  private Object[] values() {
    List<Object> vals = new ArrayList<>();
    for (Object sourceValue : sourceValues) {
      NamedParamHelp.valueAdd(vals, sourceValue);
    }
    return vals.toArray();
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    bindValues = values();
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeIn(propName, values(), not);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null && !prop.isAssocId()) {
      prop = null;
    }

    for (int i = 0; i < bindValues.length; i++) {
      if (prop == null) {
        request.addBindValue(bindValues[i]);

      } else {
        // extract the id values from the bean
        Object[] ids = prop.getAssocIdValues((EntityBean) bindValues[i]);
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

    if (bindValues.length == 0) {
      String expr = not ? "1=1" : "1=0";
      request.append(expr);
      return;
    }

    ElPropertyValue prop = getElProp(request);
    if (prop != null && !prop.isAssocId()) {
      prop = null;
    }

    if (prop != null) {
      request.append(prop.getAssocIdInExpr(propName));
      String inClause = prop.getAssocIdInValueExpr(bindValues.length);
      request.append(inClause);

    } else {
      request.append(propName);
      if (not) {
        request.append(" not");
      }
      request.append(" in (?");
      for (int i = 1; i < bindValues.length; i++) {
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
    builder.add(InExpression.class).add(propName).add(bindValues.length).add(not);
    builder.bind(bindValues.length);
  }

  @Override
  public int queryBindHash() {
    int hc = 31;
    for (int i = 0; i < bindValues.length; i++) {
      hc = 31 * hc + bindValues[i].hashCode();
    }
    return hc;
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof InExpression)) {
      return false;
    }

    InExpression that = (InExpression) other;
    return propName.equals(that.propName)
        && not == that.not
        && bindValues.length == that.bindValues.length;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    InExpression that = (InExpression) other;
    if (this.bindValues.length != that.bindValues.length) {
      return false;
    }
    for (int i = 0; i < bindValues.length; i++) {
      if (!bindValues[i].equals(that.bindValues[i])) {
        return false;
      }
    }
    return true;
  }
}
