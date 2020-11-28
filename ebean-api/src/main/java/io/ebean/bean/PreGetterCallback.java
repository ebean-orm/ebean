package io.ebean.bean;

/**
 * A callback that can be registered to fire on getter method calls.
 * It's primary purpose is to automatically flush JDBC batch buffer.
 */
public interface PreGetterCallback {

  /**
   * Trigger the callback based on a getter on a property.
   *
   * @param propertyIndex The index of the property
   */
  void preGetterTrigger(int propertyIndex);
}
