package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiNamedParam;

import java.util.Collection;
import java.util.List;

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

  /**
   * Add the potentially named parameter(s) to the values.
   */
  public static void valueAdd(List<Object> values, Object sourceValue) {

    Object value = value(sourceValue);
    if (value instanceof Collection) {
      values.addAll((Collection<?>) value);
    } else {
      values.add(value);
    }
  }
}
