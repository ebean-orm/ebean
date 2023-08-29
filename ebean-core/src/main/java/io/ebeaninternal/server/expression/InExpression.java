package io.ebeaninternal.server.expression;

import io.ebean.bean.EntityBean;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.persist.MultiValueWrapper;

import java.util.*;

public final class InExpression extends AbstractExpression implements IdInCommon {

  /**
   * Set to true when adding "1=1" predicate (due to null or empty sourceValues).
   */
  private final boolean empty;
  private final boolean not;
  private final Collection<?> sourceValues;
  private List<Object> bindValues;
  private boolean multiValueSupported;

  InExpression(String propertyName, Collection<?> sourceValues, boolean not) {
    this(propertyName, sourceValues, not, false);
  }

  InExpression(String propertyName, Collection<?> sourceValues, boolean not, boolean orEmpty) {
    super(propertyName);
    this.sourceValues = sourceValues;
    this.not = not;
    this.empty = orEmpty && (sourceValues == null || sourceValues.isEmpty());
  }

  InExpression(String propertyName, Object[] array, boolean not) {
    super(propertyName);
    this.sourceValues = Arrays.asList(array);
    this.not = not;
    this.empty = false;
  }

  public String property() {
    return propName;
  }

  @Override
  public Collection<?> idValues() {
    if (bindValues == null) {
      bindValues = new ArrayList<>(sourceValues);
    }
    return bindValues;
  }

  @Override
  public int removeIds(Set<Object> hitIds) {
    bindValues.removeAll(hitIds);
    return bindValues.size();
  }

  private List<Object> values() {
    if (empty || sourceValues == null) {
      return Collections.emptyList();
    }
    List<Object> vals = new ArrayList<>(sourceValues.size());
    for (Object sourceValue : sourceValues) {
      assert sourceValue != null : "null is not allowed in in-queries";
      NamedParamHelp.valueAdd(vals, sourceValue);
    }
    return vals;
  }

  private List<Object> initBindValues() {
    if (bindValues == null) {
      bindValues = values();
    }
    return bindValues;
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache for NOT IN or when "empty"
    if (not || empty) {
      return false;
    }
    return data.matchIn(propName, initBindValues());
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    initBindValues();
    if (bindValues.size() > 0) {
      multiValueSupported = request.isMultiValueSupported(bindValues.get(0).getClass());
    }
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    if (empty) {
      return;
    }
    for (Object value : bindValues) {
      if (value == null) {
        throw new NullPointerException("null values in 'in(...)' queries must be handled separately!");
      }
    }
    ElPropertyValue prop = getElProp(request);
    List<Object> values = bindValues;
    if (prop != null && prop.isAssocId()) {
      values = new ArrayList<>();
      for (Object bindValue : bindValues) {
        // extract the id values from the bean
        Object[] ids = prop.assocIdValues((EntityBean) bindValue);
        if (ids != null) {
          Collections.addAll(values, ids);
        }
      }
    }
    if (values.isEmpty()) {
      // nothing in in-query
      return;
    }
    if (prop != null) {
      if (prop.isDbEncrypted()) {
        // bind the key as well as the value
        request.addBindEncryptKey(prop.beanProperty().encryptKey().getStringValue());
      } else if (prop.isLocalEncrypted()) {
        List<Object> encValues = new ArrayList<>(values.size());
        for (Object value : values) {
          encValues.add(prop.localEncrypt(value));
        }
        // this is most likely binary garbage, so don't add it to the bind log
        request.addBindEncryptKey(new MultiValueWrapper(encValues));
        return;
      }
    }
    // wrap in a multi value wrapper, later the binder will decide the bind strategy to use
    request.addBindValue(new MultiValueWrapper(values));
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    if (empty) {
      request.append(SQL_TRUE);
      return;
    }
    if (bindValues.isEmpty()) {
      request.append(not ? SQL_TRUE : SQL_FALSE);
      return;
    }
    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        request.parse(prop.assocIdInExpr(propName));
        request.append(prop.assocIdInValueExpr(not, bindValues.size()));
        return;
      }
      if (prop.isDbEncrypted()) {
        request.parse(prop.beanProperty().decryptProperty(propName));
        request.appendInExpression(not, bindValues);
        return;
      }
    }
    request.property(propName);
    request.appendInExpression(not, bindValues);
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
    if (empty) {
      builder.append("empty");
    } else {
      builder.append(" ?");
      if (!multiValueSupported || bindValues.isEmpty()) {
        // query plan specific to the number of parameters in the IN clause
        builder.append(bindValues.size());
      }
    }
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(bindValues.size());
    for (Object bindValue : bindValues) {
      key.add(bindValue);
    }
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
