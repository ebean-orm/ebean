package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebeaninternal.api.SpiNamedParam;

public class ONamedParam implements SpiNamedParam {

  private final String name;

  private Object value;

  public ONamedParam(String name) {
    this.name = name;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }
}
