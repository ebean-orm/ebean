package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.SpiNamedParam;

/**
 * Helper for evaluating named parameters.
 */
class NamedParamHelp {

  /**
   * Return the bind value taking into account named parameters.
   */
  static Object value(Object val) {
    if (val instanceof SpiNamedParam) {
      return ((SpiNamedParam) val).getValue();
    }
    return val;
  }

  /**
   * Return the value as a string.
   */
  static String valueAsString(Object val) {
    Object value = value(val);
    return (value == null) ? null : value.toString();
  }

}
