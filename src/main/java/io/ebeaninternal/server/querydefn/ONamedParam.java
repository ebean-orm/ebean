package io.ebeaninternal.server.querydefn;

import io.ebeaninternal.api.SpiNamedParam;

import javax.persistence.PersistenceException;

/**
 * Named parameter used as placeholder in expressions created by EQL language parsing.
 */
class ONamedParam implements SpiNamedParam {

  private final String name;

  private Object value;

  /**
   * Create with the given name.
   */
  ONamedParam(String name) {
    this.name = name;
  }

  /**
   * Set the bind value for this named parameter.
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * Return the bind value for this named parameter.
   */
  @Override
  public Object getValue() {
    return value;
  }

  /**
   * Check the bind value has been set (so does not support null value).
   */
  void checkValueSet() {
    if (value == null) {
      throw new PersistenceException("Named parameter [" + name + "] has not had it's value set.");
    }
  }
}
