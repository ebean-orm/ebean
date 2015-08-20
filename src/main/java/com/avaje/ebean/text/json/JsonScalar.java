package com.avaje.ebean.text.json;

import java.io.IOException;

/**
 * Writes any scalar type known to Ebean the Jackson generator.
 */
public interface JsonScalar {

  void write(String name, Object value) throws IOException;
}
