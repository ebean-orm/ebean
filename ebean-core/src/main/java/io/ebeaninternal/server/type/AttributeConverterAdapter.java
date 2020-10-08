package io.ebeaninternal.server.type;

import io.ebean.config.ScalarTypeConverter;

import javax.persistence.AttributeConverter;

/**
 * Adapter from ScalarTypeConverter to AttributeConverter.
 */
class AttributeConverterAdapter<B,S> implements ScalarTypeConverter<B, S> {

  private final AttributeConverter<B,S> converter;

  AttributeConverterAdapter(AttributeConverter<B, S> converter) {
    this.converter = converter;
  }

  @Override
  public B getNullValue() {
    return null;
  }

  @Override
  public B wrapValue(S dbValue) {
    return converter.convertToEntityAttribute(dbValue);
  }

  @Override
  public S unwrapValue(B beanType) {
    return converter.convertToDatabaseColumn(beanType);
  }
}
