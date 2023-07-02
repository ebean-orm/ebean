package io.ebean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
final class DInTuples implements InTuples {

  private final String[] properties;
  private final List<Entry> entries = new ArrayList<>();


  DInTuples(String[] properties) {
    this.properties = properties;
  }

  /**
   */
  @Override
  public InTuples add(Object... values) {
    entries.add(new DEntry(values));
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
  public List<Entry> entries() {
    return Collections.unmodifiableList(entries);
  }

  /**
   * A pair of 2 value objects.
   * <p>
   * Used to support inPairs() expression.
   */
  static final class DEntry implements InTuples.Entry {

    private final Object[] vals;

    DEntry(Object[] vals) {
      this.vals = vals;
    }

    @Override
    public Object[] values() {
      return vals;
    }
  }
}
