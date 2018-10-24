package org.tests.model.json;

public class StringJacksonType implements BasicJacksonType<String> {

  private String value;

  public StringJacksonType() {}

  public StringJacksonType(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
