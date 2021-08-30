package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;

/**
 * Base ScalarType object.
 */
abstract class ScalarTypeBase<T> implements ScalarType<T> {

  protected final Class<T> type;
  protected final boolean jdbcNative;
  protected final int jdbcType;

  ScalarTypeBase(Class<T> type, boolean jdbcNative, int jdbcType) {
    this.type = type;
    this.jdbcNative = jdbcNative;
    this.jdbcType = jdbcType;
  }

  @Override
  public long asVersion(T value) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean isBinaryType() {
    // override for binary/byte based types
    return false;
  }

  /**
   * Default implementation of mutable false.
   */
  @Override
  public boolean isMutable() {
    return false;
  }

  /**
   * Default to true.
   */
  @Override
  public boolean isDirty(Object value) {
    return true;
  }

  /**
   * Just return 0.
   */
  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public boolean isJdbcNative() {
    return jdbcNative;
  }

  @Override
  public int getJdbcType() {
    return jdbcType;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String format(Object value) {
    return formatValue((T) value);
  }

  @Override
  public void loadIgnore(DataReader reader) {
    reader.incrementPos(1);
  }

}
