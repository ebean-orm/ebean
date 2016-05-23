package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.server.deploy.DeployParser;
import com.avaje.ebeaninternal.server.persist.Binder;
import com.avaje.ebeaninternal.server.type.DataBind;

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
  static class NullValue extends Value {
    @Override
    public String bindClause() {
      return "=null";
    }
  }

  /**
   * Set property to a simple value.
   */
  static class SimpleValue extends Value {

    final Object value;

    SimpleValue(Object value) {
      this.value = value;
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
      binder.bindObject(dataBind, value);
    }
  }

  /**
   * Set using an expression with no bind value.
   */
  static class NoneValue extends Value {
    @Override
    public String bindClause() {
      return "";
    }
  }

  /**
   * Set using an expression with many bind values.
   */
  static class RawArrayValue extends Value {

    final Object[] bindValues;

    public RawArrayValue(Object[] bindValues) {
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
      }
    }
  }

  /**
   * The set properties/expressions and their bind values.
   */
  private LinkedHashMap<String, Value> values = new LinkedHashMap<String, Value>();

  /**
   * Normal set property.
   */
  public void set(String propertyName, Object value) {
    if (value == null) {
      values.put(propertyName, NULL_VALUE);

    } else {
      values.put(propertyName, new SimpleValue(value));
    }
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
  public void setRaw(String propertyExpression, Object... vals) {
    if (vals.length == 0) {
      setRaw(propertyExpression);
    } else {
      values.put(propertyExpression, new RawArrayValue(vals));
    }
  }

  /**
   * Return true if this update has the same logical set clause.
   */
  public boolean isSameByPlan(OrmUpdateProperties that) {
    return that.values.size() == values.size()
        && logicalSetClause().equals(that.logicalSetClause());
  }

  /**
   * Build the hash for the query plan caching.
   */
  public void buildQueryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(OrmUpdateProperties.class);
    Set<Map.Entry<String, Value>> entries = values.entrySet();
    for (Map.Entry<String, Value> entry : entries) {
      builder.add(entry.getKey());
      builder.bind(entry.getValue().getBindCount());
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
      sb.append(deployParser.parse(property).replace("${}", ""));
      sb.append(entry.getValue().bindClause());
    }
    return sb.toString();
  }

  /**
   * Return a logical set clause to use for isSameByPlan() use.
   */
  private String logicalSetClause() {

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Value> entry : values.entrySet()) {
      sb.append(", ");
      sb.append(entry.getKey()).append(entry.getValue().bindClause());
    }
    return sb.toString();
  }

}
