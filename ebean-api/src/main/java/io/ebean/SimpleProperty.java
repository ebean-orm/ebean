package io.ebean;

final class SimpleProperty implements Query.Property {

  private final String expression;

  SimpleProperty(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return expression;
  }
}
