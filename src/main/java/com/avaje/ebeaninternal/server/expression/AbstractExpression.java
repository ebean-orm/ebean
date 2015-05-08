package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Base class for simple expressions.
 * 
 * @author rbygrave
 */
public abstract class AbstractExpression implements SpiExpression {

  private static final long serialVersionUID = 4072786211853856174L;

  protected final String propName;

  protected AbstractExpression(String propName) {
    this.propName = propName;
  }

  public String getPropertyName() {
    return propName;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    String propertyName = getPropertyName();
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

  protected ElPropertyValue getElProp(SpiExpressionRequest request) {

    String propertyName = getPropertyName();
    return request.getBeanDescriptor().getElGetValue(propertyName);
  }
}
