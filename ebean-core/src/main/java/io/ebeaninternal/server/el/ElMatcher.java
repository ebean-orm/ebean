package io.ebeaninternal.server.el;

/**
 * Interface for defining matches for filter expressions.
 */
public interface ElMatcher<T> {

  /**
   * Return true if the bean matches the expression.
   */
  boolean isMatch(T bean);
}
