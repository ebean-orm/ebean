package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlBeanLoad;

public class DynamicPropertyAggregationFormulaMTO extends DynamicPropertyAggregationFormula {

  private final BeanPropertyAssocOne prop;

  DynamicPropertyAggregationFormulaMTO(BeanPropertyAssocOne prop, String name, String parsedFormula, boolean aggregate, BeanProperty asTarget, String alias) {
    super(name, prop.getIdScalarType(), parsedFormula, aggregate, asTarget, alias);
    this.prop = prop;
  }

  @Override
  public boolean isAggregationManyToOne() {
    return true;
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {
    Object value;
    try {
      value = prop.read(sqlBeanLoad.ctx());
    } catch (Exception e) {
      sqlBeanLoad.ctx().handleLoadError(fullName, e);
      return;
    }
    if (asTarget != null) {
      sqlBeanLoad.load(asTarget, value);
    }
  }

}
