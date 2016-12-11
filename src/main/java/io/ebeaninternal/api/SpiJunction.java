package io.ebeaninternal.api;

import io.ebean.Junction;
import io.ebeaninternal.server.expression.DocQueryContext;

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
