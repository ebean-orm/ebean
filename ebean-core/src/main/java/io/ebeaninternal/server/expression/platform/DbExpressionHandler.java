package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.server.expression.BitwiseOp;
import io.ebeaninternal.server.expression.Op;

/**
 * Adds the db platform specific json expression.
 */
public interface DbExpressionHandler {

  /**
   * Write the db platform specific json expression.
   */
  void json(DbExpressionRequest request, String propName, String path, Op operator, Object value);

  /**
   * Add SQL for ARRAY CONTAINS expression.
   */
  void arrayContains(DbExpressionRequest request, String propName, boolean contains, Object... values);

  /**
   * Add SQL for ARRAY IS EMPTY expression.
   */
  void arrayIsEmpty(DbExpressionRequest request, String propName, boolean empty);

  /**
   * Add the bitwise expression.
   */
  void bitwise(DbExpressionRequest request, String propName, BitwiseOp operator, long flags, String compare, long match);

  /**
   * Performs a "CONCAT" operation for that platform.
   */
  String concat(String property0, String separator, String property1, String suffix);
}
