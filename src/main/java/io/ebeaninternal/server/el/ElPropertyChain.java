package io.ebeaninternal.server.el;

import io.ebean.bean.EntityBean;
import io.ebean.text.StringParser;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.lib.util.StringHelper;
import io.ebeaninternal.server.query.SplitName;
import io.ebeaninternal.server.type.ScalarType;

import java.util.Arrays;


/**
 * A ElGetValue based on a chain of properties.
 * <p>
 * Used to get the value for an compound expression like customer.name or
 * customer.shippingAddress.city etc.
 * </p>
 * <p>
 * Note that if any element in the chain returns null, then null is returned and
 * no further processing of the chain occurs.
 * </p>
 */
public class ElPropertyChain implements ElPropertyValue {

  private final String prefix;

  private final String placeHolder;
  private final String placeHolderEncrypted;

  private final String name;

  private final String expression;

  private final boolean containsMany;

  private final ElPropertyValue[] chain;

  private final boolean assocId;
  private final int last;
  private final BeanProperty lastBeanProperty;
  private final ScalarType<?> scalarType;

  private final ElPropertyValue lastElPropertyValue;

  public ElPropertyChain(boolean containsMany, boolean embedded, String expression, ElPropertyValue[] chain) {

    this.containsMany = containsMany;
    this.chain = chain;
    this.expression = expression;
    int dotPos = expression.lastIndexOf('.');
    if (dotPos > -1) {
      this.name = expression.substring(dotPos + 1);
      if (embedded) {
        int embPos = expression.lastIndexOf('.', dotPos - 1);
        this.prefix = embPos == -1 ? null : expression.substring(0, embPos);

      } else {
        this.prefix = expression.substring(0, dotPos);
      }
    } else {
      this.prefix = null;
      this.name = expression;
    }

    this.assocId = chain[chain.length - 1].isAssocId();

    this.last = chain.length - 1;
    this.lastBeanProperty = chain[chain.length - 1].getBeanProperty();
    if (lastBeanProperty != null) {
      this.scalarType = lastBeanProperty.getScalarType();
    } else {
      // case for nested compound type (non-scalar)
      this.scalarType = null;
    }
    this.lastElPropertyValue = chain[chain.length - 1];
    this.placeHolder = getElPlaceHolder(prefix, lastElPropertyValue, false);
    this.placeHolderEncrypted = getElPlaceHolder(prefix, lastElPropertyValue, true);
  }

  public String toString() {
    return "expr:" + expression + " chain:" + Arrays.toString(chain);
  }

  @Override
  public boolean isAggregation() {
    return false;
  }

  private String getElPlaceHolder(String prefix, ElPropertyValue lastElPropertyValue, boolean encrypted) {
    if (prefix == null) {
      return lastElPropertyValue.getElPlaceholder(encrypted);
    }

    String el = lastElPropertyValue.getElPlaceholder(encrypted);

    if (!el.contains("${}")) {
      // typically a secondary table property
      return StringHelper.replaceString(el, "${", "${" + prefix + ".");
    } else {
      return StringHelper.replaceString(el, ROOT_ELPREFIX, "${" + prefix + "}");
    }
  }

  /**
   * Return true if there is a many property from sinceProperty to
   * the end of this chain.
   */
  public boolean containsManySince(String sinceProperty) {
    if (sinceProperty == null) {
      return containsMany;
    }
    if (!expression.startsWith(sinceProperty)) {
      return containsMany;
    }

    int i = 1 + SplitName.count(sinceProperty);

    for (; i < chain.length; i++) {
      if (chain[i].getBeanProperty().containsMany()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean containsFormulaWithJoin() {
    // Not cascading the check at this stage
    return false;
  }

  public boolean containsMany() {
    return containsMany;
  }

  public String getElPrefix() {
    return prefix;
  }

  public String getName() {
    return name;
  }

  public String getElName() {
    return expression;
  }

  public String getElPlaceholder(boolean encrypted) {
    return encrypted ? placeHolderEncrypted : placeHolder;
  }

  public boolean isDbEncrypted() {
    return lastElPropertyValue.isDbEncrypted();
  }

  public boolean isLocalEncrypted() {
    return lastElPropertyValue.isLocalEncrypted();
  }

  @Override
  public String getAssocIsEmpty(SpiExpressionRequest request, String path) {
    return lastElPropertyValue.getAssocIsEmpty(request, path);
  }

  public Object[] getAssocIdValues(EntityBean bean) {
    // Don't navigate the object graph as bean
    // is assumed to be the appropriate type
    return lastElPropertyValue.getAssocIdValues(bean);
  }

  public String getAssocIdExpression(String prefix, String operator) {
    return lastElPropertyValue.getAssocIdExpression(expression, operator);
  }

  public String getAssocIdInExpr(String prefix) {
    return lastElPropertyValue.getAssocIdInExpr(prefix);
  }

  public String getAssocIdInValueExpr(int size) {
    return lastElPropertyValue.getAssocIdInValueExpr(size);
  }

  @Override
  public boolean isAssocMany() {
    return lastElPropertyValue.isAssocMany();
  }

  public boolean isAssocId() {
    return assocId;
  }

  public boolean isAssocProperty() {
    for (ElPropertyValue aChain : chain) {
      if (aChain.isAssocProperty()) {
        return true;
      }
    }
    return false;
  }

  public String getDbColumn() {
    return lastElPropertyValue.getDbColumn();
  }

  public BeanProperty getBeanProperty() {
    return lastBeanProperty;
  }


  public boolean isDateTimeCapable() {
    return scalarType != null && scalarType.isDateTimeCapable();
  }

  public int getJdbcType() {
    return scalarType == null ? 0 : scalarType.getJdbcType();
  }

  public Object parseDateTime(long systemTimeMillis) {
    return scalarType.convertFromMillis(systemTimeMillis);
  }

  public StringParser getStringParser() {
    return scalarType;
  }

  public Object convert(Object value) {
    // just convert using the last one in the chain
    return lastElPropertyValue.convert(value);
  }

  @Override
  public Object pathGet(Object bean) {
    for (ElPropertyValue aChain : chain) {
      bean = aChain.pathGet(bean);
      if (bean == null) {
        return null;
      }
    }
    return bean;
  }

  @Override
  public Object pathGetNested(Object bean) {

    Object prevBean = bean;
    for (int i = 0; i < last; i++) {
      // always return non null prevBean
      prevBean = chain[i].pathGetNested(prevBean);
    }
    // try the last step in the chain
    return chain[last].pathGet(prevBean);
  }

  @Override
  public void pathSet(Object bean, Object value) {

    Object prevBean = bean;
    for (int i = 0; i < last; i++) {
      prevBean = chain[i].pathGetNested(prevBean);
    }
    if (prevBean != null) {
      if (lastBeanProperty != null) {
        // last chain element maps to a real scalar property
        lastBeanProperty.pathSet(prevBean, value);

      } else {
        // a non-scalar property of a Compound value object
        lastElPropertyValue.pathSet(prevBean, value);
      }
    }
  }


}
