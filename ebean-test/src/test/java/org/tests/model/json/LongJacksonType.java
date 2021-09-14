package org.tests.model.json;

public class LongJacksonType implements BasicJacksonType<Long> {

  private Long value;

  public LongJacksonType() {}

  public LongJacksonType(Long value) {
    this.value = value;
  }

  @Override
  public Long getValue() {
    return value;
  }

  public void setValue(Long value) {
    this.value = value;
  }

}
