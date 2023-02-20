package io.ebean;

final class SimpleProperty<T> implements Query.Property<T> {

  private final String expression;

  SimpleProperty(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return expression;
  }
}
