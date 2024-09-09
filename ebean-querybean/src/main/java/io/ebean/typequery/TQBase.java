package io.ebean.typequery;

import io.ebean.ExpressionList;

/**
 * Base methods used internally to support TQMany expressions.
 */
interface TQBase<R> {

  /**
   * Return the current ExpressionList.
   */
  @SuppressWarnings("rawtypes")
  ExpressionList _expr();

  /**
   * Return the property name.
   */
  String _name();

  /**
   * Return the query root.
   */
  R _root();
}
