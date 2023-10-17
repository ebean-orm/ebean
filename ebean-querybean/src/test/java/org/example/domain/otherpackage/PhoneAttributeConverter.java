package org.example.domain.otherpackage;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PhoneAttributeConverter implements AttributeConverter<PhoneNumber, String> {
  @Override
  public String convertToDatabaseColumn(final PhoneNumber attribute) {
    return attribute.getMsisdn();
  }

  @Override
  public PhoneNumber convertToEntityAttribute(final String dbData) {
    return new PhoneNumber(dbData);
  }
}
