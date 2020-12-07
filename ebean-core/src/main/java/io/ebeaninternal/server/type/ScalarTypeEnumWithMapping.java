package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;

import javax.persistence.EnumType;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Additional control over mapping to DB values.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ScalarTypeEnumWithMapping extends ScalarTypeEnumStandard.EnumBase implements ScalarType, ScalarTypeEnum {

  private final EnumToDbValueMap beanDbMap;

  private final int length;

  private final boolean withConstraint;

  /**
   * Create with an explicit mapping of bean to database values.
   */
  public ScalarTypeEnumWithMapping(EnumToDbValueMap<?> beanDbMap, Class<?> enumType, int length, boolean withConstraint) {
    super(enumType, false, beanDbMap.getDbType());
    this.beanDbMap = beanDbMap;
    this.length = length;
    this.withConstraint = withConstraint;
  }

  public ScalarTypeEnumWithMapping(EnumToDbValueMap<?> beanDbMap, Class<?> enumType, int length) {
    this(beanDbMap, enumType, length, true);
  }

  @Override
  public boolean isCompatible(EnumType enumType) {
    return enumType == null;
  }

  @Override
  public long asVersion(Object value) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean isBinaryType() {
    return false;
  }

  /**
   * Return the IN values for DB constraint construction.
   */
  @Override
  public Set<String> getDbCheckConstraintValues() {
    if (!withConstraint) {
      return null;
    }
    LinkedHashSet values = new LinkedHashSet();
    Iterator<?> it = beanDbMap.dbValues();
    while (it.hasNext()) {
      Object dbValue = it.next();
      if (beanDbMap.isIntegerType()) {
        values.add(dbValue.toString());
      } else {
        values.add("'" + dbValue.toString() + "'");
      }
    }
    return values;
  }

  /**
   * Return the DB column length for storing the enum value.
   */
  @Override
  public int getLength() {
    return length;
  }

  @Override
  public void bind(DataBinder binder, Object value) throws SQLException {
    beanDbMap.bind(binder, value);
  }

  @Override
  public Object read(DataReader reader) throws SQLException {
    return beanDbMap.read(reader);
  }

  @Override
  public Object toBeanType(Object dbValue) {
    if (dbValue == null || dbValue instanceof Enum<?>) {
      return dbValue;
    }
    return beanDbMap.getBeanValue(dbValue);
  }

  @Override
  public Object toJdbcType(Object beanValue) {
    return beanDbMap.getDbValue(beanValue);
  }

}
