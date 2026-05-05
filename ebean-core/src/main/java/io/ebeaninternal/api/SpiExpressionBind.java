package io.ebeaninternal.api;

import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Expression bind values capture.
 */
public interface SpiExpressionBind {

  /**
   * Return the bean descriptor for the root type.
   */
  BeanDescriptor<?> descriptor();

  /**
   * Add an encryption key to bind to this request.
   */
  void addBindEncryptKey(Object encryptKey);

  /**
   * Add a bind value to this request.
   */
  void addBindValue(Object bindValue);

  /**
   * Escapes a string to use it as exact match in Like clause.
   */
  String escapeLikeString(String value);
}
