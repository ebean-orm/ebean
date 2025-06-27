package io.ebeaninternal.api;

import io.ebean.bean.EntityBean;
import java.util.ArrayList;
import java.util.List;

/**
 * BindValues used for L2 query cache key matching.
 * <p>
 * The equals/hashCode implementation must meet the requirement that the query bind values
 * match for L2 query cache hit (given the query plan hash is already a match).
 */
public final class BindValuesKey {

  private final List<Object> values = new ArrayList<>();

  private final SpiEbeanServer server;

  public BindValuesKey(SpiEbeanServer server) {
    this.server = server;
  }

  /**
   * Add a bind value.
   */
  public BindValuesKey add(Object value) {
    if (value instanceof EntityBean) {
      // Only interested in id to keep the memory footprint low
      Object id = server.beanId(value);
      if (id != null) {
        value = id;
      }
    }
    values.add(value);
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof BindValuesKey && ((BindValuesKey) obj).values.equals(values);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }


}
