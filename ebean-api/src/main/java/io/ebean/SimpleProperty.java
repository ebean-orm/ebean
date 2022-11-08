package io.ebean;

final class SimpleProperty<BT> implements Query.Property<BT> {

  private final String expression;

  SimpleProperty(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return expression;
  }
}
