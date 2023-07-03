package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlBeanLoad;

import java.util.Set;

@SuppressWarnings("rawtypes")
public final class DynamicPropertyAggregationFormulaMTO extends DynamicPropertyAggregationFormula {

  private final BeanPropertyAssocOne prop;
  private final Set<String> includes;

  DynamicPropertyAggregationFormulaMTO(BeanPropertyAssocOne prop, String name, String parsedFormula, boolean aggregate, BeanProperty asTarget, String alias, Set<String> includes) {
    super(name, prop.idScalarType(), parsedFormula, aggregate, asTarget, alias);
    this.prop = prop;
    this.includes = includes;
  }

  @Override
  public boolean isAggregationManyToOne() {
    return true;
  }

  @Override
  public void extraIncludes(Set<String> predicateIncludes) {
    predicateIncludes.addAll(includes);
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
