package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SplitName;

/**
 * Base class for simple expressions.
 */
public abstract class AbstractExpression implements SpiExpression {

  protected final String propName;

  protected AbstractExpression(String propName) {
    this.propName = propName;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // override on SimpleExpression
    return null;
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return propertyNestedPath(propName, desc);
  }

  protected String propertyNestedPath(String propertyName, BeanDescriptor<?> desc) {
    if (propertyName != null) {
      ElPropertyDeploy elProp = desc.getElPropertyDeploy(propertyName);
      if (elProp != null && elProp.containsMany()) {
        return SplitName.begin(propName);
      }
    }
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    propertyContainsMany(propName, desc, manyWhereJoin);
  }

  /**
   * Check the logical property path for containing a 'many' property.
   */
  protected void propertyContainsMany(String propertyName, BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    if (propertyName != null) {
      ElPropertyDeploy elProp = desc.getElPropertyDeploy(propertyName);
      if (elProp != null) {
        if (elProp.containsFormulaWithJoin()) {
          // for findRowCount query select clause
          manyWhereJoin.addFormulaWithJoin(propertyName);
        }
        if (elProp.containsMany()) {
          // for findRowCount we join to a many property
          manyWhereJoin.add(elProp);
        }
      }
    }
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    // do nothing
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    validation.validate(propName);
  }

  protected final ElPropertyValue getElProp(SpiExpressionRequest request) {

    return request.getBeanDescriptor().getElGetValue(propName);
  }
}
