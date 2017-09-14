package io.ebeaninternal.server.expression;

import io.ebean.bean.EntityBean;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.MultiValueWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class InExpression extends AbstractExpression {

  private final boolean not;

  private final Collection<?> sourceValues;

  private Object[] bindValues;

  private boolean containsNull;

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

  private void prepareBindValues() {
    Set<Object> vals = new HashSet<>(sourceValues.size());
    for (Object sourceValue : sourceValues) {
      NamedParamHelp.valueAdd(vals, sourceValue);
    }
    containsNull = vals.remove(null);
    bindValues = vals.toArray();
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    prepareBindValues();
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    prepareBindValues();
    context.writeIn(propName, bindValues, not, containsNull);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (Object value : bindValues) {
      if (value == null) {
        throw new NullPointerException("null values in 'in(...)' queries must be handled separately!");
      }
    }
    ElPropertyValue prop = getElProp(request);
    if (prop != null && !prop.isAssocId()) {
      prop = null;
    }

    if (prop == null) {
      if (bindValues.length > 0) {
        // if we have no property, we wrap them in a multi value wrapper.
        // later the binder will decide, which bind strategy to use.
        request.addBindValue(new MultiValueWrapper(Arrays.asList(bindValues)));
      }
    } else {
      List<Object> idList = new ArrayList<>();
      for (Object bindValue : bindValues) {
        // extract the id values from the bean
        Object[] ids = prop.getAssocIdValues((EntityBean) bindValue);
        if (ids != null) {
          for (Object id : ids) {
            idList.add(id);
          }
        }
      }
      if (!idList.isEmpty()) {
        request.addBindValue(new MultiValueWrapper(idList));
      }
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    if (bindValues.length == 0) {
      if (containsNull) {
        String expr = not ? " is not null" : " is null";
        request.append(propName).append(expr);
      } else {
        String expr = not ? "1=1" : "1=0";
        request.append(expr);
      }
      return;
    }

    ElPropertyValue prop = getElProp(request);
    if (prop != null && !prop.isAssocId()) {
      prop = null;
    }

    if (containsNull != not) {
      request.append("(");
    }
    
    String realPropName = propName;
    if (prop != null) {
      
      realPropName = prop.getAssocIdInExpr(propName);
      request.append(realPropName);
      String inClause = prop.getAssocIdInValueExpr(bindValues.length);
      if (not) {
        request.append(" not");
      }
      request.append(inClause);

    } else {
      request.append(realPropName);
      if (not) {
        request.append(" not");
      }
      request.appendInExpression(bindValues);
    }

    if (containsNull != not) {
      request.append("or ").append(realPropName).append(" is null) ");
    }
  }

  /**
   * Based on the number of values in the in clause.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    if (not) {
      builder.append("NotIn[");
    } else {
      builder.append("In[");
    }
    builder.append(propName);
    builder.append(" ?").append(bindValues.length);
    if (containsNull) {
      builder.append(",null");
    }
    builder.append("]");
  }

  @Override
  public int queryBindHash() {
    int hc = 92821;
    for (Object bindValue : bindValues) {
      hc = 92821 * hc + bindValue.hashCode();
    }
    return hc;
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
