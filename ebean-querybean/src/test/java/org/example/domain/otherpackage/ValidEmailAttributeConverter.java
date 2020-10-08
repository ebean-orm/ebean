package org.example.domain.otherpackage;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ValidEmailAttributeConverter implements AttributeConverter<ValidEmail, String> {
  @Override
  public String convertToDatabaseColumn(final ValidEmail attribute) {
    return attribute.getEmailAddress();
  }

  @Override
  public ValidEmail convertToEntityAttribute(final String dbData) {
    return new ValidEmail(dbData);
  }
}
