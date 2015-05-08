package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Additional control over mapping to DB values.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ScalarTypeEnumWithMapping extends ScalarTypeEnumStandard.EnumBase implements ScalarType, ScalarTypeEnum {

  private final EnumToDbValueMap beanDbMap;

  private final int length;

  /**
   * Create with an explicit mapping of bean to database values.
   */
  public ScalarTypeEnumWithMapping(EnumToDbValueMap<?> beanDbMap, Class<?> enumType, int length) {
    super(enumType, false, beanDbMap.getDbType());
    this.beanDbMap = beanDbMap;
    this.length = length;
  }

  /**
   * Return the IN values for DB constraint construction.
   */
  public String getConstraintInValues() {

    StringBuilder sb = new StringBuilder();

    int i = 0;

    sb.append("(");

    Iterator<?> it = beanDbMap.dbValues();
    while (it.hasNext()) {
      Object dbValue = it.next();
      if (i++ > 0) {
        sb.append(",");
      }
      if (!beanDbMap.isIntegerType()) {
        sb.append("'");
      }
      sb.append(dbValue.toString());
      if (!beanDbMap.isIntegerType()) {
        sb.append("'");
      }
    }

    sb.append(")");

    return sb.toString();
  }

  /**
   * Return the DB column length for storing the enum value.
   * <p>
   * This is for enum's mapped to strings.
   * </p>
   */
  public int getLength() {
    return length;
  }

  public void bind(DataBind b, Object value) throws SQLException {
    beanDbMap.bind(b, value);
  }

  public Object read(DataReader dataReader) throws SQLException {
    return beanDbMap.read(dataReader);
  }

  public Object toBeanType(Object dbValue) {
    if (dbValue == null || dbValue instanceof Enum<?>) {
      return dbValue;
    }
    return beanDbMap.getBeanValue(dbValue);
  }

  public Object toJdbcType(Object beanValue) {
    return beanDbMap.getDbValue(beanValue);
  }

}
