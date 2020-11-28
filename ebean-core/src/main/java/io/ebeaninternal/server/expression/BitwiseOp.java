package io.ebeaninternal.server.expression;

/**
 * Bitwise expression operator.
 */
public enum BitwiseOp {

  /**
   * All flags are set (derived expression BITAND flags == flags).
   */
  ALL,

  /**
   * Any flags are set (derived expression BITAND flags != 0).
   */
  ANY,

  /**
   * BITAND flags == match.
   */
  AND
}
