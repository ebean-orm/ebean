package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.expression.Op;

/**
 * Adds the db platform specific json expression.
 */
public interface JsonExpressionHandler {


  /**
   * Write the db platform specific json expression.
   */
  void addSql(SpiExpressionRequest request, String propName, String path, Op operator, Object value);

}
