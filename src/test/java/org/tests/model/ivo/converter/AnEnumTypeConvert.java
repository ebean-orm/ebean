package org.tests.model.ivo.converter;

import io.ebean.config.ScalarTypeConverter;

public class AnEnumTypeConvert implements ScalarTypeConverter<AnEnumType, String> {

  @Override
  public AnEnumType getNullValue() {
    return null;
  }

  @Override
  public AnEnumType wrapValue(String value) {
    return AnEnumType.valueOf(value);
  }

  @Override
  public String unwrapValue(AnEnumType beanType) {
    return beanType.name();
  }
}
