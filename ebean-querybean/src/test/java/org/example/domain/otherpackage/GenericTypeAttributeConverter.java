package org.example.domain.otherpackage;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class GenericTypeAttributeConverter implements AttributeConverter<GenericType<GenericTypeArgument>, String> {
  @Override
  public String convertToDatabaseColumn(final GenericType<GenericTypeArgument> attribute) {
    return attribute.getData().name();
  }

  @Override
  public GenericType<GenericTypeArgument> convertToEntityAttribute(final String dbData) {
    return new GenericType<>(GenericTypeArgument.valueOf(dbData));
  }
}
