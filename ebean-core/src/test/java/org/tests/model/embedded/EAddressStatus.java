package org.tests.model.embedded;

public enum EAddressStatus {

  ONE("One") {
    @Override
    public String doIt() {
      return 10 + name;
    }
  },
  TWO("Two") {
    @Override
    public String doIt() {
      return 20 + name;
    }
  };

  String name;

  EAddressStatus(String name) {
    this.name = name;
  }
  public abstract String doIt();
}
