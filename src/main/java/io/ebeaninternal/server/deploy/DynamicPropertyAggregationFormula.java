package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlBeanLoad;
import io.ebeaninternal.server.type.ScalarType;

import javax.persistence.PersistenceException;

/**
 * Dynamic property based on aggregation (max, min, avg, count).
 */
class DynamicPropertyAggregationFormula extends DynamicPropertyBase {

  private final String parsedFormula;

  private final boolean aggregate;

  private final BeanProperty asTarget;

  private final String alias;

  DynamicPropertyAggregationFormula(String name, ScalarType<?> scalarType, String parsedFormula, boolean aggregate, BeanProperty asTarget, String alias) {
    super(name, name, null, scalarType);
    this.parsedFormula = parsedFormula;
    this.aggregate = aggregate;
    this.asTarget = asTarget;
    this.alias = alias;
  }

  @Override
  public String toString() {
    return "DynamicPropertyFormula[" + parsedFormula + "]";
  }

  @Override
  public boolean isAggregation() {
    return aggregate;
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
    ctx.appendParseSelect(parsedFormula, alias);
  }

}
