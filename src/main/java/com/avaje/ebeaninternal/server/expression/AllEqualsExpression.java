package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

import java.util.Map;
import java.util.Map.Entry;

class AllEqualsExpression extends NonPrepareExpression {

  private static final long serialVersionUID = -8691773558205937025L;

  private final Map<String, Object> propMap;

  AllEqualsExpression(Map<String, Object> propMap) {
    this.propMap = propMap;
  }

  protected String name(String propName) {
    return propName;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    if (propMap != null) {
      for (String propertyName : propMap.keySet()) {
        ElPropertyDeploy elProp = desc.getElPropertyDeploy(name(propertyName));
        if (elProp != null && elProp.containsMany()) {
          manyWhereJoin.add(elProp);
        }
      }
    }
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    for (String propName: propMap.keySet()) {
      validation.validate(propName);
    }
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    if (propMap.isEmpty()) {
      return;
    }
    for (Object value : propMap.values()) {
      // null value uses is null clause
      if (value != null) {
        request.addBindValue(value);
      }
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    if (propMap.isEmpty()) {
      return;
    }

    request.append("(");

    int count = 0;
    for (Map.Entry<String, Object> entry : propMap.entrySet()) {

      Object value = entry.getValue();
      String propName = entry.getKey();

      if (count > 0) {
        request.append("and ");
      }

      request.append(name(propName));
      if (value == null) {
        request.append(" is null ");
      } else {
        request.append(" = ? ");
      }
      count++;
    }
    request.append(")");
  }

  /**
   * Based on the properties and whether they are null.
   * <p>
   * The null check is required due to the "is null" sql being generated.
   * </p>
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {

    builder.add(AllEqualsExpression.class);

    for (Entry<String, Object> entry : propMap.entrySet()) {
      Object value = entry.getValue();
      String propName = entry.getKey();
      builder.add(propName).add(value == null ? 0 : 1);
      builder.bind(value == null ? 0 : 1);
    }
  }

  @Override
  public int queryBindHash() {

    int hc = 31;
    for (Object value : propMap.values()) {
      hc = hc * 31 + (value == null ? 0 : value.hashCode());
    }

    return hc;
  }
}
