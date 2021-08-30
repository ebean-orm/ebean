package io.ebeaninternal.server.persist;

import java.util.Collection;

/**
 * Wraps the multi values that are used for "property in (...)" queries
 * @author Roland Praml, FOCONIS AG
 */
public final class MultiValueWrapper {
  private final Collection<?> values;
  private final Class<?> type;

  public MultiValueWrapper(Collection<?> values, Class<?> type) {
    this.values = values;
    this.type = type;
  }

  public MultiValueWrapper(Collection<?> values) {
    this.values = values;
    this.type = values.iterator().next().getClass();
  }

  public Collection<?> getValues() {
    return values;
  }

  public Class<?> getType() {
    return type;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Array[" + values.size() + "]={");
    for (Object value : values) {
      sb.append(value).append(',');
      if (sb.length() > 50) {
        sb.append("...}");
        return sb.toString();
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append('}');
    return sb.toString();
  }
}
