package org.example.otherpackage

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class ValidEmailAttributeConverter : AttributeConverter<ValidEmail, String> {
  override fun convertToDatabaseColumn(attribute: ValidEmail): String {
    return attribute.emailAddress
  }

  override fun convertToEntityAttribute(dbData: String): ValidEmail {
    return ValidEmail(dbData)
  }
}
