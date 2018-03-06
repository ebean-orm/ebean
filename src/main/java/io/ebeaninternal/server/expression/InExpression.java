package io.ebeaninternal.server.expression;

import io.ebean.bean.EntityBean;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.MultiValueWrapper;
import io.ebeaninternal.api.NaturalKeyQueryData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class InExpression extends AbstractExpression {

  private final boolean not;

  private final Collection<?> sourceValues;

  private List<Object> bindValues;

  private boolean multiValueSupported;

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

  private List<Object> values() {
    List<Object> vals = new ArrayList<>(sourceValues.size());
    for (Object sourceValue : sourceValues) {
      assert sourceValue != null : "null is not allowed in in-queries";
      NamedParamHelp.valueAdd(vals, sourceValue);
    }
    return vals;
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache for NOT IN
    return !not && data.matchIn(propName, bindValues);
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    bindValues = values();
    if (bindValues.size() > 0) {
      multiValueSupported = request.isMultiValueSupported((bindValues.get(0)).getClass());
    }
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeIn(propName, values().toArray(), not);
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
      if (bindValues.size() > 0) {
        // if we have no property, we wrap them in a multi value wrapper.
        // later the binder will decide, which bind strategy to use.
        request.addBindValue(new MultiValueWrapper(bindValues));
      }
    } else {
      List<Object> idList = new ArrayList<>();
      for (Object bindValue : bindValues) {
        // extract the id values from the bean
        Object[] ids = prop.getAssocIdValues((EntityBean) bindValue);
        if (ids != null) {
          Collections.addAll(idList, ids);
        }
      }
      if (!idList.isEmpty()) {
        request.addBindValue(new MultiValueWrapper(idList));
      }
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    if (bindValues.isEmpty()) {
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
      String inClause = prop.getAssocIdInValueExpr(not, bindValues.size());
      request.append(inClause);

    } else {
      request.append(propName);
      request.appendInExpression(not, bindValues);
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
    builder.append(" ?");
    if (!multiValueSupported) {
      // query plan specific to the number of parameters in the IN clause
      builder.append(bindValues.size());
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
    if (this.bindValues.size() != that.bindValues.size()) {
      return false;
    }
    for (int i = 0; i < bindValues.size(); i++) {
      if (!bindValues.get(i).equals(that.bindValues.get(i))) {
        return false;
      }
    }
    return true;
  }
}
