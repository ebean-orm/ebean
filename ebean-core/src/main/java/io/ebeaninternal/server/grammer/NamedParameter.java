package io.ebeaninternal.server.grammer;

public class NamedParameter {

  public static final String PREFIX = "$namedParam$";

  private final String name;

  public NamedParameter(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
