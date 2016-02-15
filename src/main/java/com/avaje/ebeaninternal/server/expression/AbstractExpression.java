package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Base class for simple expressions.
 */
public abstract class AbstractExpression implements SpiExpression {

  private static final long serialVersionUID = 4072786211853856174L;

  protected final String propName;

  protected AbstractExpression(String propName) {
    this.propName = propName;
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    if (propName != null) {
      ElPropertyDeploy elProp = desc.getElPropertyDeploy(propName);
      if (elProp != null) {
        if (elProp.containsFormulaWithJoin()) {
          // for findRowCount query select clause
          manyWhereJoin.addFormulaWithJoin(propName);
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
