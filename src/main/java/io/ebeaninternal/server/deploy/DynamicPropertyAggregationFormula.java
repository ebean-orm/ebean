package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlBeanLoad;
import io.ebeaninternal.server.type.DataReader;
import io.ebeaninternal.server.type.ScalarType;

import java.util.Map;

import javax.persistence.PersistenceException;

/**
 * Dynamic property based on aggregation (max, min, avg, count).
 */
class DynamicPropertyAggregationFormula extends DynamicPropertyBase {

  private final String parsedFormula;

  private final boolean aggregate;

  final BeanProperty asTarget;

  private final String alias;

  private final String mapKey;

  DynamicPropertyAggregationFormula(String name, ScalarType<?> scalarType, String parsedFormula, boolean aggregate, BeanProperty asTarget, String alias) {
    super(name, name, null, scalarType);
    this.parsedFormula = parsedFormula;
    this.aggregate = aggregate;
    this.asTarget = asTarget;
    if (alias == null) {
      this.alias = null;
      this.mapKey = null;
    } else {
      int pos = alias.indexOf('@');
      if (pos == -1) {
        this.alias = alias;
        this.mapKey = null;
      } else {
        this.alias = alias.substring(0, pos);
        if (asTarget != null && Map.class.isAssignableFrom(asTarget.getPropertyType())) {
          this.mapKey = name.substring(pos + asTarget.getName().length() + 2);
        } else {
          this.mapKey = null;
        }
      }
    }
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
      value = scalarType.read(sqlBeanLoad.ctx().getDataReader());
    } catch (Exception e) {
      sqlBeanLoad.ctx().handleLoadError(null, asTarget, fullName, e);
      return;
    }
    if (asTarget != null) {
      if (mapKey != null) {
        sqlBeanLoad.loadInMap(asTarget, mapKey, value);
      } else {
        sqlBeanLoad.load(asTarget, value);
      }
    }
  }

  @Override
  public void loadOptional(SqlBeanLoad sqlBeanLoad) {
    load(sqlBeanLoad);
  }

  @Override
  public void appendSelect(DbSqlContext ctx, boolean subQuery) {
    ctx.appendParseSelect(parsedFormula, alias);
  }

}
