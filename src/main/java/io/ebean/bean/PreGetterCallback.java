package io.ebean.bean;

/**
 * A callback that can be registered to fire on getter method calls.
 * It's primary purpose is to automatically flush JDBC batch buffer.
 */
public interface PreGetterCallback {

  /**
   * Trigger the callback.
   */
  void preGetterTrigger();
}
