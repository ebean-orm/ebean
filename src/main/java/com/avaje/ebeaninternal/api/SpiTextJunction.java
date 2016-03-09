package com.avaje.ebeaninternal.api;

import com.avaje.ebean.TextJunction;
import com.avaje.ebeaninternal.server.expression.DocQueryContext;

import java.io.IOException;

/**
 * SPI extension to the full text junctions (MUST, MUST NOT, SHOULD).
 */
public interface SpiTextJunction<T> extends TextJunction<T> {

  /**
   * Write the junction expression to the query context.
   */
  void writeDocQueryJunction(DocQueryContext context) throws IOException;

}
