package io.ebean.core.type;

/**
 * Base ScalarType object.
 */
public abstract class ScalarTypeBase<T> implements ScalarType<T> {

  protected final Class<T> type;
  protected final boolean jdbcNative;
  protected final int jdbcType;

  public ScalarTypeBase(Class<T> type, boolean jdbcNative, int jdbcType) {
    this.type = type;
    this.jdbcNative = jdbcNative;
    this.jdbcType = jdbcType;
  }

  @Override
  public boolean jdbcNative() {
    return jdbcNative;
  }

  @Override
  public int jdbcType() {
    return jdbcType;
  }

  @Override
  public Class<T> type() {
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
