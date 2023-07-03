package io.ebean;

import io.ebean.service.SpiInTuples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of SpiInTuples.
 */
final class DInTuples implements SpiInTuples {

  private final String[] properties;
  private final int propertyCount;
  private final List<Object[]> entries = new ArrayList<>();


  DInTuples(String[] properties) {
    this.properties = properties;
    this.propertyCount = properties.length;
  }

  @Override
  public InTuples add(Object... values) {
    if (values.length != propertyCount) {
      throw new IllegalArgumentException("Require " + propertyCount + " values but got " + values.length);
    }
    entries.add(values);
    return this;
  }

  /**
   * Return the first property name.
   */
  @Override
  public String[] properties() {
    return properties;
  }

  /**
   * Return all the value pairs.
   */
  @Override
  public List<Object[]> entries() {
    return Collections.unmodifiableList(entries);
  }

}
