package io.ebeaninternal.server.el;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarType;
import io.ebean.plugin.Property;
import io.ebean.text.StringParser;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.deploy.BeanProperty;

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
public final class ElPropertyChain implements ElPropertyValue {

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
    this.lastBeanProperty = chain[chain.length - 1].beanProperty();
    if (lastBeanProperty != null) {
      this.scalarType = lastBeanProperty.scalarType();
    } else {
      // case for nested compound type (non-scalar)
      this.scalarType = null;
    }
    this.lastElPropertyValue = chain[chain.length - 1];
    this.placeHolder = placeHolder(prefix, lastElPropertyValue, false);
    this.placeHolderEncrypted = placeHolder(prefix, lastElPropertyValue, true);
  }

  @Override
  public String toString() {
    return "expr:" + expression + " chain:" + Arrays.toString(chain);
  }

  @Override
  public int fetchPreference() {
    return chain[0].fetchPreference();
  }

  @Override
  public boolean isAggregation() {
    return false;
  }

  private String placeHolder(String prefix, ElPropertyValue lastElPropertyValue, boolean encrypted) {
    if (prefix == null) {
      return lastElPropertyValue.elPlaceholder(encrypted);
    }
    String el = lastElPropertyValue.elPlaceholder(encrypted);
    if (!el.contains("${}")) {
      // typically a secondary table property
      return el.replace("${", "${" + prefix + ".");
    } else {
      return el.replace(ROOT_ELPREFIX, "${" + prefix + "}");
    }
  }

  /**
   * Return true if there is a many property from sinceProperty to
   * the end of this chain.
   */
  @Override
  public boolean containsManySince(String sinceProperty) {
    if (sinceProperty == null) {
      return containsMany;
    }
    if (!expression.startsWith(sinceProperty)) {
      return containsMany;
    }
    int i = 1 + SplitName.count(sinceProperty);
    for (; i < chain.length; i++) {
      if (chain[i].beanProperty().containsMany()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsFormulaWithJoin() {
    return lastBeanProperty.containsFormulaWithJoin();
  }

  @Override
  public boolean containsMany() {
    return containsMany;
  }

  @Override
  public String elPrefix() {
    return prefix;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String elName() {
    return expression;
  }

  @Override
  public String elPlaceholder(boolean encrypted) {
    return encrypted ? placeHolderEncrypted : placeHolder;
  }

  @Override
  public boolean isDbEncrypted() {
    return lastElPropertyValue.isDbEncrypted();
  }

  @Override
  public boolean isLocalEncrypted() {
    return lastElPropertyValue.isLocalEncrypted();
  }

  @Override
  public Object localEncrypt(Object value) {
    return lastElPropertyValue.localEncrypt(value);
  }

  @Override
  public String assocIsEmpty(SpiExpressionRequest request, String path) {
    return lastElPropertyValue.assocIsEmpty(request, path);
  }

  @Override
  public Object[] assocIdValues(EntityBean bean) {
    // Don't navigate the object graph as bean
    // is assumed to be the appropriate type
    return lastElPropertyValue.assocIdValues(bean);
  }

  @Override
  public String assocIdExpression(String prefix, String operator) {
    return lastElPropertyValue.assocIdExpression(expression, operator);
  }

  @Override
  public String assocIdInExpr(String prefix) {
    return lastElPropertyValue.assocIdInExpr(prefix);
  }

  @Override
  public String assocIdInValueExpr(boolean not, int size) {
    return lastElPropertyValue.assocIdInValueExpr(not, size);
  }

  @Override
  public boolean isAssocMany() {
    return lastElPropertyValue.isAssocMany();
  }

  @Override
  public Property property() {
    return lastBeanProperty;
  }

  @Override
  public boolean isAssocId() {
    return assocId;
  }

  @Override
  public boolean isAssocProperty() {
    for (ElPropertyValue aChain : chain) {
      if (aChain.isAssocProperty()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String dbColumn() {
    return lastElPropertyValue.dbColumn();
  }

  @Override
  public BeanProperty beanProperty() {
    return lastBeanProperty;
  }

  @Override
  public int jdbcType() {
    return scalarType == null ? 0 : scalarType.jdbcType();
  }

  @Override
  public StringParser stringParser() {
    return scalarType;
  }

  @Override
  public Object convert(Object value) {
    // just convert using the last one in the chain
    return lastElPropertyValue.convert(value);
  }

  @Override
  public Object pathGet(Object bean) {
    for (ElPropertyValue aChain : chain) {
      if (aChain.isAssocMany()) {
        throw new UnsupportedOperationException("pathGet not supported on [" + expression + "], because " + aChain + " is an assocMany property");
      }
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
