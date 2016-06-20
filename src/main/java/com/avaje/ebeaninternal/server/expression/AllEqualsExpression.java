package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class AllEqualsExpression extends NonPrepareExpression {

  private final Map<String, Object> propMap;

  AllEqualsExpression(Map<String, Object> propMap) {
    this.propMap = propMap;
  }

  protected String name(String propName) {
    return propName;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeAllEquals(propMap);
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
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
      builder.bindIfNotNull(value);
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

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof AllEqualsExpression)) {
      return false;
    }

    AllEqualsExpression that = (AllEqualsExpression) other;
    return isSameByValue(that, false);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    if (!(other instanceof AllEqualsExpression)) {
      return false;
    }

    AllEqualsExpression that = (AllEqualsExpression) other;
    return isSameByValue(that, true);
  }

  private boolean isSameByValue(AllEqualsExpression that, boolean byValue) {

    if (propMap.size() != that.propMap.size()) {
      return false;
    }

    Iterator<Entry<String, Object>> thisIt = propMap.entrySet().iterator();
    Iterator<Entry<String, Object>> thatIt = that.propMap.entrySet().iterator();

    while (thisIt.hasNext() && thatIt.hasNext()) {
      Entry<String, Object> thisNext = thisIt.next();
      Entry<String, Object> thatNext = thatIt.next();

      if (!thisNext.getKey().equals(thatNext.getKey())) {
        return false;
      }
      if (!Same.sameBy(byValue, thisNext.getValue(), thatNext.getValue())) {
        return false;
      }
    }

    return true;
  }
}
