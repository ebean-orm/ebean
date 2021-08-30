package io.ebeaninternal.server.querydefn;

public final class NaturalKeyBindParam {

  private final String name;
  private final Object value;

  public NaturalKeyBindParam(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

}
