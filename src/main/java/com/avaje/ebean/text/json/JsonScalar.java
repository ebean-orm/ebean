package com.avaje.ebean.text.json;

import java.io.IOException;

/**
 * Writes any scalar type known to Ebean to the underlying Jackson generator.
 */
public interface JsonScalar {

  /**
   * Write the scalar type to JSON where the value can be any type known to Ebean
   * including Enums, Java8 time types, Joda types, URL, URI etc.
   */
  void write(Object value) throws IOException;
}
