package io.ebeaninternal.server.type;

import io.ebeaninternal.json.ModifyAwareOwner;

/**
 * Base ScalarType object for mutable types .
 */
public abstract class ScalarTypeBaseMutable<T> extends ScalarTypeBase<T> {

  public ScalarTypeBaseMutable(Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public T deepCopy(T in) {
    // the copy is done by converting it to JSON and back.
    // this is the safe and easiest way now (not the fastest)
    return parse(formatValue(in));
  }

  @Override
  public boolean isModified(T originalValue, T currentValue) {
    // some shortcuts:
    // value2 is the 'current' value, that can be a ModifyAwareOwner
    if (originalValue.equals(currentValue)) {
      return false;
    } else if (currentValue instanceof ModifyAwareOwner && ((ModifyAwareOwner) currentValue).isMarkedDirty()) {
      return true; // bail out here, if marked as dirty
    } else {
      // comparison is done by converting the JSON strings.
      // this is the safe and easiest way now (not the fastest)
      return !formatValue(originalValue).equals(formatValue(currentValue));
    }
  }


}
