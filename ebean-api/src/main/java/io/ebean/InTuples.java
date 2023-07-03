package io.ebean;

import java.util.List;

/**
 * IN expression using multiple columns.
 * <p>
 * Produces SQL expression in the form of (A,B,C) IN ((a0,b0,c0), (a1,b1,c1), ... )
 * where A,B,C are the properties in the tuples.
 */
public interface InTuples {

  /**
   * Create given the properties in the tuples.
   */
  static InTuples of(String... properties) {
    return new DInTuples(properties);
  }

  /**
   * Create given the properties in the tuples.
   */
  static InTuples of(Query.Property<?>... properties) {
    String[] props = new String[properties.length];
    for (int i = 0; i < properties.length; i++) {
      props[i] = properties[i].toString();
    }
    return new DInTuples(props);
  }

  /**
   * Add a tuple entry. All values must be non-null.
   */
  InTuples add(Object... values);

}
