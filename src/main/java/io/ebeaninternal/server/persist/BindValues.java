package io.ebeaninternal.server.persist;

import java.util.ArrayList;

/**
 * Holds a list of bind values for binding to a PreparedStatement.
 */
public class BindValues {

  final ArrayList<Value> list = new ArrayList<>();

  /**
   * Create with a Binder.
   */
  public BindValues() {
  }

  /**
   * Return the number of bind values.
   */
  public int size() {
    return list.size();
  }

  /**
   * Add a bind value with its JDBC datatype.
   *
   * @param value  the bind value
   * @param dbType the type as per java.sql.Types
   */
  public void add(Object value, int dbType, String name) {
    list.add(new Value(value, dbType, name));
  }

  /**
   * List of bind values.
   */
  public ArrayList<Value> values() {
    return list;
  }

  /**
   * A Value has additionally the JDBC data type.
   */
  public static class Value {

    private final Object value;

    private final int dbType;

    private final String name;

    /**
     * Create the value.
     */
    public Value(Object value, int dbType, String name) {
      this.value = value;
      this.dbType = dbType;
      this.name = name;
    }

    /**
     * Return the type as per java.sql.Types.
     */
    public int getDbType() {
      return dbType;
    }

    /**
     * Return the value.
     */
    public Object getValue() {
      return value;
    }

    /**
     * Return the property name.
     */
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}
