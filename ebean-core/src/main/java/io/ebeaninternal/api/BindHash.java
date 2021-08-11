package io.ebeaninternal.api;

import java.util.ArrayList;
import java.util.List;

/**
 * BindHash implementation.
 */
public class BindHash {

  private final List<Object> values = new ArrayList<>();

  public BindHash update(Object value) {
    values.add(value);
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof BindHash && ((BindHash) obj).values.equals(values);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }


}
