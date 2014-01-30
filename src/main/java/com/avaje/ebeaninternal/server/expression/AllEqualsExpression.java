package com.avaje.ebeaninternal.server.expression;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

class AllEqualsExpression implements SpiExpression {

  private static final long serialVersionUID = -8691773558205937025L;

  private final Map<String, Object> propMap;

  AllEqualsExpression(Map<String, Object> propMap) {
    this.propMap = propMap;
  }

  protected String name(String propName) {
    return propName;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    if (propMap != null) {
      Iterator<String> it = propMap.keySet().iterator();
      while (it.hasNext()) {
        String propertyName = it.next();
        ElPropertyDeploy elProp = desc.getElPropertyDeploy(name(propertyName));
        if (elProp != null && elProp.containsMany()) {
          manyWhereJoin.add(elProp);
        }
      }
    }
  }

  public void addBindValues(SpiExpressionRequest request) {

    if (propMap.isEmpty()) {
      return;
    }
    Iterator<Object> it = propMap.values().iterator();
    while (it.hasNext()) {
      Object value = it.next();
      if (value != null) {
        request.addBindValue(value);
      } else {
        // null value uses is null clause
      }
    }
  }

  public void addSql(SpiExpressionRequest request) {

    if (propMap.isEmpty()) {
      return;
    }

    request.append("(");

    Set<Entry<String, Object>> entries = propMap.entrySet();
    Iterator<Entry<String, Object>> it = entries.iterator();

    int count = 0;
    while (it.hasNext()) {
      Map.Entry<java.lang.String, java.lang.Object> entry = it.next();
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
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {

    builder.add(AllEqualsExpression.class);

    for (Entry<String, Object> entry :  propMap.entrySet()) {
      Object value = entry.getValue();
      String propName = entry.getKey();
      builder.add(propName).add(value == null ? 0 : 1);
      builder.bind(value == null ? 0 : 1);      
    }    
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    queryAutoFetchHash(builder);
  }

  public int queryBindHash() {
    
    int hc = 31;
    for (Object value : propMap.values()) {
      hc = hc * 31 + Objects.hashCode(value);      
    }

    return hc;
  }
}
