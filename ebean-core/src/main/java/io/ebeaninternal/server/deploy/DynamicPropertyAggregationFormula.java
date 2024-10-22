package io.ebeaninternal.server.deploy;

import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.query.SqlBeanLoad;

import jakarta.persistence.PersistenceException;

/**
 * Dynamic property based on aggregation (max, min, avg, count).
 */
class DynamicPropertyAggregationFormula extends DynamicPropertyBase {

  private final String parsedFormula;
  private final boolean aggregate;
  final BeanProperty asTarget;
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
    return "DynamicPropertyFormula " + parsedFormula;
  }

  @Override
  public boolean isAggregation() {
    return aggregate;
  }

  @Override
  public Object read(DataReader dataReader) {
    try {
      return scalarType.read(dataReader);
    } catch (Exception e) {
      throw new PersistenceException("Error loading on " + fullName, e);
    }
  }

  @Override
  public void load(SqlBeanLoad sqlBeanLoad) {
    Object value;
    try {
      value = scalarType.read(sqlBeanLoad.ctx().dataReader());
    } catch (Exception e) {
      sqlBeanLoad.ctx().handleLoadError(fullName, e);
      return;
    }
    if (asTarget != null) {
      sqlBeanLoad.load(asTarget, value);
    }
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    ctx.appendParseSelect(parsedFormula, alias);
  }

  @Override
  public void appendGroupBy(DbSqlContext ctx, boolean subQuery) {
    ctx.appendParseSelect(parsedFormula, null);
  }

  @Override
  public boolean isLobForPlatform() {
    return false;
  }

}
