package io.ebeaninternal.server.expression;

import io.ebean.bean.EntityBean;
import io.ebean.config.dbplatform.MultiValueBinder;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.ArrayWrapper;
import io.ebeaninternal.server.persist.Binder;

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
    List<Object> vals = new ArrayList<>(sourceValues.size());
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

    Binder binder = request.getBinder();
    if (prop == null && binder.getMultiValueBinder() != null) {
      if (bindValues.length > 0) {
        // if the binder supports multi value mode and we have at least one bind value
        // wrap them in an array wrapper
        request.addBindValue(new ArrayWrapper(bindValues));
      }
    } else {
      for (Object bindValue : bindValues) {
        if (prop == null) {
          request.addBindValue(bindValue);

        } else {
          // extract the id values from the bean
          Object[] ids = prop.getAssocIdValues((EntityBean) bindValue);
          if (ids != null) {
            for (Object id : ids) {
              request.addBindValue(id);
            }
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
      if (not) {
        request.append(" not");
      }
      request.append(inClause);

    } else {
      request.append(propName);
      if (not) {
        request.append(" not");
      }
      MultiValueBinder mvBinder = request.getBinder().getMultiValueBinder();
      if (mvBinder == null) {
        request.append(" in (?");
        for (int i = 1; i < bindValues.length; i++) {
          request.append(", ").append("?");
        }
        request.append(" ) ");
      } else {
        request.append(" in (");
        request.append(mvBinder.getPlaceholder(bindValues.length));
        request.append(" ) ");
      }
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
    builder.append(" ?").append(bindValues.length).append("]");
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
