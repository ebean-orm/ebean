package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebeaninternal.api.SpiNamedParam;

import javax.persistence.PersistenceException;

/**
 * Named parameter used as placeholder in expressions created by EQL language parsing.
 */
public class ONamedParam implements SpiNamedParam {

  private final String name;

  private Object value;

  /**
   * Create with the given name.
   */
  public ONamedParam(String name) {
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
  public Object getValue() {
    return value;
  }

  /**
   * Check the bind value has been set (so does not support null value).
   */
  public void checkValueSet() {
    if (value == null) {
      throw new PersistenceException("Named parameter ["+name+"] has not had it's value set.");
    }
  }
}
