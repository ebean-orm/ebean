package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebean.util.SplitName;
import io.ebeaninternal.api.NaturalKeyQueryData;

/**
 * Base class for simple expressions.
 */
public abstract class AbstractExpression implements SpiExpression {

  protected final String propName;

  protected AbstractExpression(String propName) {
    this.propName = propName;
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // by default can't use naturalKey cache
    return false;
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
          // for findCount query select clause
          manyWhereJoin.addFormulaWithJoin(propertyName);
        }
        if (elProp.containsMany()) {
          // for findCount we join to a many property
          manyWhereJoin.add(elProp);
          if (elProp.isAggregation()) {
            manyWhereJoin.setAggregation();
          }
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
