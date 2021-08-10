package io.ebeaninternal.api;

/**
 * BindHash implementation.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public interface BindHash {

  /**
   * Update with boolean value.
   */
  BindHash update(boolean boolValue);

  /**
   * Update with int value.
   */
  BindHash update(int intValue);

  /**
   * Update with long value.
   */
  BindHash update(long longValue);

  /**
   * Update with object value.
   */
  BindHash update(Object value);

  /**
   * finishes the hash. May be used to compute internal state. After finish, no
   * update method must be called
   */
  void finish();

}
