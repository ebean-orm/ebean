package org.tests.types;

import io.ebean.config.ScalarTypeConverter;

public class ScalarTypeEncryptedStringConverter implements ScalarTypeConverter<EncryptedString, String> {

  @Override
  public EncryptedString getNullValue() {
    return null;
  }

  @Override
  public EncryptedString wrapValue(final String scalarType) {
    return new EncryptedString(scalarType);
  }

  @Override
  public String unwrapValue(final EncryptedString beanType) {
    return beanType.getEncryptedData();
  }

}
