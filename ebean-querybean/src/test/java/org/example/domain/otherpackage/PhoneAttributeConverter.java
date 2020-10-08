package org.example.domain.otherpackage;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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
