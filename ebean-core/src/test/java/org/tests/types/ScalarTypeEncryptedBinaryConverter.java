package org.tests.types;

import io.ebean.config.ScalarTypeConverter;

public class ScalarTypeEncryptedBinaryConverter implements ScalarTypeConverter<EncryptedBinary, byte[]> {


  @Override
  public EncryptedBinary getNullValue() {
    return null;
  }

  @Override
  public EncryptedBinary wrapValue(final byte[] scalarType) {
    return new EncryptedBinary(scalarType);
  }

  @Override
  public byte[] unwrapValue(final EncryptedBinary beanType) {
    return beanType.getEncryptedData();
  }

}
