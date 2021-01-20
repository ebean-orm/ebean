package io.ebeaninternal.server.querydefn;

import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.DeployParser;
import io.ebeaninternal.server.persist.Binder;
import io.ebeaninternal.server.type.DataBind;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Set properties for a UpdateQuery.
 */
public class OrmUpdateProperties {

  private static final NullValue NULL_VALUE = new NullValue();

  private static final NoneValue NONE_VALUE = new NoneValue();

  /**
   * Bind value used in the set clause for update query.
   * It may/may not have bind values etc.
   */
  public static abstract class Value {

    public void bind(Binder binder, DataBind dataBind) throws SQLException {
      // default to no bind values
    }

    public String bindClause() {
      return "";
    }

    public int getBindCount() {
      return 0;
    }
  }

  /**
   * Set property to null.
   */
  private static class NullValue extends Value {
    @Override
    public String bindClause() {
      return "=null";
    }
  }

  /**
   * Set property to a simple value.
   */
  private static class SimpleValue extends Value {

    final Object value;
    final ScalarType<Object> scalarType;

    SimpleValue(Object value, ScalarType<Object> scalarType) {
      this.value = value;
      this.scalarType = scalarType;
    }

    @Override
    public int getBindCount() {
      return 1;
    }

    @Override
    public String bindClause() {
      return "=?";
    }

    @Override
    public void bind(Binder binder, DataBind dataBind) throws SQLException {
      if (scalarType != null) {
        scalarType.bind(dataBind, value);
      } else {
        binder.bindObject(dataBind, value);
      }
      dataBind.append(value).append(",");
    }
  }

  /**
   * Set using an expression with no bind value.
   */
  private static class NoneValue extends Value {
    @Override
    public String bindClause() {
      return "";
    }
  }

  /**
   * Set using an expression with many bind values.
   */
  private static class RawArrayValue extends Value {

    final Object[] bindValues;

    RawArrayValue(Object[] bindValues) {
      this.bindValues = bindValues;
    }

    @Override
    public int getBindCount() {
      return bindValues.length;
    }

    @Override
    public void bind(Binder binder, DataBind dataBind) throws SQLException {
      for (Object val : bindValues) {
        binder.bindObject(dataBind, val);
        dataBind.append(val).append(",");
      }
    }
  }

  /**
   * The set properties/expressions and their bind values.
   */
  private final LinkedHashMap<String, Value> values = new LinkedHashMap<>();

  public void set(String propertyName, Object value, ScalarType<Object> scalarType) {
    values.put(propertyName, new SimpleValue(value, scalarType));
  }

  public void setNull(String propertyName) {
    values.put(propertyName, NULL_VALUE);
  }

  /**
   * Set a raw expression with no bind values.
   */
  public void setRaw(String propertyName) {
    values.put(propertyName, NONE_VALUE);
  }

  /**
   * Set a raw expression with many bind values.
   */
  void setRaw(String propertyExpression, Object... vals) {
    if (vals.length == 0) {
      setRaw(propertyExpression);
    } else {
      values.put(propertyExpression, new RawArrayValue(vals));
    }
  }

  /**
   * Build the hash for the query plan caching.
   */
  void buildQueryPlanHash(StringBuilder builder) {
    Set<Map.Entry<String, Value>> entries = values.entrySet();
    for (Map.Entry<String, Value> entry : entries) {
      builder.append("key:").append(entry.getKey());
      builder.append(" ?:").append(entry.getValue().getBindCount());
    }
  }

  /**
   * Bind all the bind values for the update set clause.
   */
  public void bind(Binder binder, DataBind dataBind) throws SQLException {
    for (Value bindValue : values.values()) {
      bindValue.bind(binder, dataBind);
    }
  }

  /**
   * Build the actual set clause converting logical property names to db columns etc.
   */
  public String buildSetClause(DeployParser deployParser) {
    int setCount = 0;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Value> entry : values.entrySet()) {
      String property = entry.getKey();
      if (setCount++ > 0) {
        sb.append(", ");
      }
      // translate to db columns and remove table alias placeholders
      sb.append(trim(deployParser.parse(property)));
      sb.append(entry.getValue().bindClause());
    }
    return sb.toString();
  }

  static String trim(String dbColumn) {
    return dbColumn.replace("${}", "");
  }

}
