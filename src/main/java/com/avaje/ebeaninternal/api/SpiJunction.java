package com.avaje.ebeaninternal.api;

import com.avaje.ebean.Junction;
import com.avaje.ebeaninternal.server.expression.DocQueryContext;

import java.io.IOException;

/**
 * SPI methods for Junction.
 */
public interface SpiJunction<T> extends Junction<T> {

  /**
   * Write the Junction taking into account it is implied.
   */
  void writeDocQueryJunction(DocQueryContext context) throws IOException;
}
