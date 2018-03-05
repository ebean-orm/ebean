package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlBeanLoad;
import io.ebeaninternal.server.type.ScalarType;

import javax.persistence.PersistenceException;

/**
 * Dynamic property based on aggregation (max, min, avg, count).
 */
class DynamicPropertyAggregationFormula extends DynamicPropertyBase {

  private final String parsedAggregation;

  private final BeanProperty asTarget;

  DynamicPropertyAggregationFormula(String name, ScalarType<?> scalarType, String parsedAggregation, BeanProperty asTarget) {
    super(name, name, null, scalarType);
    this.parsedAggregation = parsedAggregation;
    this.asTarget = asTarget;
  }

  @Override
  public String toString() {
    return "DynamicPropertyFormula[" + parsedAggregation + "]";
  }

  @Override
  public boolean isAggregation() {
    return true;
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {

    try {
      Object value = scalarType.read(sqlBeanLoad.ctx().getDataReader());
      if (asTarget != null) {
        sqlBeanLoad.load(asTarget, value);
      }

    } catch (Exception e) {
      throw new PersistenceException("Error loading on " + fullName, e);
    }
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    ctx.appendParseSelect(parsedAggregation);
  }

}
