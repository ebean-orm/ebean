package io.ebeaninternal.server.persist.platform;

import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.type.ScalarType;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Default implementation for multi value help.
 */
public class MultiValueBind {


  @FunctionalInterface
  public interface BindOne {
    void bind(Object value) throws SQLException;
  }

  protected Object[] toArray(Collection<?> values, ScalarType<?> type) {
    Object[] array = new Object[values.size()];
    int i = 0;
    for (Object value : values) {
      array[i++] = type.toJdbcType(value);
    }
    return array;
  }

  /**
   * Defaults to not supported and using a bind value per element.
   */
  public boolean isSupported() {
    return false;
  }

  /**
   * Defaults to not supported and using a bind value per element.
   */
  public boolean isTypeSupported(int jdbcType) {
    return false;
  }

  /**
   * Default for multi values. They are appended one by one.
   */
  public void bindMultiValues(DataBind dataBind, Collection<?> values, ScalarType<?> type, BindOne bindOne) throws SQLException {
    for (Object value : values) {
      if (!type.isJdbcNative()) {
        value = type.toJdbcType(value);
      }
      bindOne.bind(value);
    }
  }

  /**
   * Appends the 'in' expression to the request. Must add leading & trailing space!
   */
  public String getInExpression(boolean not, ScalarType<?> type, int size) {
    StringBuilder sb = new StringBuilder();
    if (not) {
      sb.append(" not");
    }
    sb.append(" in (?");
    for (int i = 1; i < size; i++) {
      sb.append(", ").append("?");
    }
    sb.append(" ) ");
    return sb.toString();
  }
}
